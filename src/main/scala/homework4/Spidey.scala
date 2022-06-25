package homework4

import homework4.html.HtmlUtils
import homework4.http.*
import homework4.math.Monoid

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

case class SpideyConfig(
  maxDepth: Int,
  sameDomainOnly: Boolean = true,
  tolerateErrors: Boolean = true,
  retriesOnError: Int = 0
)

case class CrawlingResult[O](links: List[String], output: O)

class Spidey(httpClient: HttpClient)(using ExecutionContext):
  private def linksOf(url: String, response: HttpResponse, sameDomainOnly: Boolean): List[String] =
    if response.contentType.exists(_.mimeType == ContentType.HtmlMimeType) then
      HtmlUtils
        .linksOf(response.body, url)
        .filter(HttpUtils.isUrlHttp)
        .filter(link => !sameDomainOnly || HttpUtils.sameDomain(url, link))
        .distinct
    else List.empty

  private def retry(retries: Int)(urlRetriever: => Future[HttpResponse]): Future[HttpResponse] =
    urlRetriever.transformWith {
      case Success(response) if !response.isServerError => Future.successful(response)
      case Success(_) | Failure(NonFatal(_)) if retries > 0 => retry(retries - 1)(urlRetriever)
      case result => Future.fromTry(result)
    }

  def crawl[O: Monoid](url: String, config: SpideyConfig)(processor: Processor[O]): Future[O] =
    def crawl(urls: List[String], depth: Int, visitedUrls: Set[String], output: O): Future[O] =
      Future.traverse(urls)(crawlSingleUrl).flatMap { results =>
        val outputs = results.map(_.output)
        val combinedOutput = output |+| Monoid.sum(outputs)

        if depth == config.maxDepth then Future.successful(combinedOutput)
        else
          val currentLevelLinks = results.flatMap(_.links).distinct
          val urlsToVisit = currentLevelLinks.filterNot(visitedUrls)
          val updatedVisitedUrls = visitedUrls ++ currentLevelLinks

          crawl(urlsToVisit, depth + 1, updatedVisitedUrls, combinedOutput)
      }

    def crawlSingleUrl(url: String): Future[CrawlingResult[O]] =
      (for
        response <- retry(config.retriesOnError)(httpClient.get(url))
        output <- processor(url, response)
      yield CrawlingResult(linksOf(url, response, config.sameDomainOnly), output)) recover {
        case NonFatal(_) if config.tolerateErrors => CrawlingResult(List.empty, Monoid[O].identity)
      }

    crawl(List(url), 0, Set(url), Monoid[O].identity)
