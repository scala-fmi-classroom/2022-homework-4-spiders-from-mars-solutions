package homework4

import homework4.http.*
import homework4.math.Monoid

import scala.concurrent.{ExecutionContext, Future}

case class SpideyConfig(
  maxDepth: Int,
  sameDomainOnly: Boolean = true,
  tolerateErrors: Boolean = true,
  retriesOnError: Int = 0
)

class Spidey(httpClient: HttpClient)(using ExecutionContext):
  def crawl[O: Monoid](url: String, config: SpideyConfig)(processor: Processor[O]): Future[O] = ???
