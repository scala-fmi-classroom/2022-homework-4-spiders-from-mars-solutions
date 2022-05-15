package homework4.generic

import homework4.http.{AsyncHttpClient, HttpResponse}

trait GenericHttpClient:
  def get[F[_]: Concurrent](url: String): F[HttpResponse]

class GenericAsyncHttpClient extends GenericHttpClient:
  val asyncHttpClient = new AsyncHttpClient

  def get[F[_]: Concurrent](url: String): F[HttpResponse] = Concurrent[F].fromFuture {
    asyncHttpClient.get(url)
  }

  def shutdown(): Unit = asyncHttpClient.shutdown()
