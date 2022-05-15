package homework4.http

import org.asynchttpclient.Dsl.asyncHttpClient
import org.asynchttpclient.Response

import scala.concurrent.{ExecutionException, Future, Promise}
import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Try}

class AsyncHttpClient extends HttpClient:
  private val client = asyncHttpClient()

  private def toHttpResponse(response: Response): HttpResponse = new HttpResponse:
    def status: Int = response.getStatusCode
    def headers: Map[String, String] = response.getHeaders.asScala.map(es => (es.getKey.toLowerCase, es.getValue)).toMap
    def bodyAsBytes: Array[Byte] = response.getResponseBodyAsBytes

  def get(url: String): Future[HttpResponse] =
    val p = Promise[HttpResponse]()

    val eventualResponse = client.prepareGet(url).setFollowRedirect(true).execute()
    eventualResponse.addListener(
      () =>
        p.complete {
          Try(toHttpResponse(eventualResponse.get())) recoverWith { case e: ExecutionException =>
            Failure(e.getCause)
          }
        },
      null
    )

    p.future

  def shutdown(): Unit = client.close()
