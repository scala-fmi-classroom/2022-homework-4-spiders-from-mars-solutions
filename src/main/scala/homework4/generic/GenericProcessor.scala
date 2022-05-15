package homework4.generic

import homework4.http.HttpResponse

trait GenericProcessor[O]:
  def apply[F[_]: Concurrent](url: String, response: HttpResponse): F[O]
