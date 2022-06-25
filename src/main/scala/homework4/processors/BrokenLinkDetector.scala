package homework4.processors

import homework4.Processor
import homework4.http.HttpResponse

import scala.concurrent.Future

object BrokenLinkDetector extends Processor[Set[String]]:
  def apply(url: String, response: HttpResponse): Future[Set[String]] = Future.successful {
    if response.isNotFound then Set(url)
    else Set.empty[String]
  }
