package homework4

import homework4.http.HttpResponse

import scala.concurrent.Future

trait Processor[O]:
  def apply(url: String, response: HttpResponse): Future[O]
