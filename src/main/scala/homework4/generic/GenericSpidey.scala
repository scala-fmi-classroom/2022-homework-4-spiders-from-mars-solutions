package homework4.generic

import homework4.html.HtmlUtils
import homework4.http.*
import homework4.math.Monoid

import homework4.{CrawlingResult, SpideyConfig}

import scala.util.control.NonFatal
import scala.util.{Failure, Success}

class GenericSpidey[F[_]: Concurrent](httpClient: GenericHttpClient):
  private def linksOf(url: String, response: HttpResponse, sameDomainOnly: Boolean): List[String] =
    if response.contentType.exists(_.mimeType == ContentType.HtmlMimeType) then
      HtmlUtils
        .linksOf(response.body, url)
        .filter(HttpUtils.isUrlHttp)
        .filter(link => !sameDomainOnly || HttpUtils.sameDomain(url, link))
        .distinct
    else List.empty

  private def retry(retries: Int)(urlRetriever: => F[HttpResponse]): F[HttpResponse] =
    urlRetriever.transformWith[HttpResponse] {
      case Success(response) if !response.isServerError => Concurrent[F].pure(response)
      case Success(_) | Failure(NonFatal(_)) if retries > 0 => retry(retries - 1)(urlRetriever)
      case result => Concurrent[F].fromTry(result)
    }

  def crawl[O: Monoid](startingUrl: String, config: SpideyConfig)(processor: GenericProcessor[O]): F[O] =
    def crawl(urls: List[String], depth: Int, visitedUrls: Set[String], output: O): F[O] =
      Concurrent[F].parallelTraverse(urls)(crawlSingleUrl).flatMap { results =>
        val outputs = results.map(_.output)
        val combinedOutput = output |+| Monoid.sum(outputs)

        if depth == config.maxDepth then Concurrent[F].pure(combinedOutput)
        else
          val currentLevelLinks = results.flatMap(_.links).distinct
          val urlsToVisit = currentLevelLinks.filterNot(visitedUrls)
          val updatedVisitedUrls = visitedUrls ++ currentLevelLinks

          crawl(urlsToVisit, depth + 1, updatedVisitedUrls, combinedOutput)
      }

    def crawlSingleUrl(url: String): F[CrawlingResult[O]] =
      (for
        response <- retry(config.retriesOnError)(httpClient.get(url))
        output <- processor(url, response)
      yield CrawlingResult(linksOf(url, response, config.sameDomainOnly), output)) recover {
        case NonFatal(_) if config.tolerateErrors => CrawlingResult(List.empty, Monoid[O].identity)
      }

    crawl(List(startingUrl), 0, Set(startingUrl), Monoid[O].identity)
  end crawl
