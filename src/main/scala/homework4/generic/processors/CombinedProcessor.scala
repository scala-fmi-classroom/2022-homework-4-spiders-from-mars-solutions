package homework4.generic.processors

import homework4.generic.{Concurrent, GenericProcessor}
import homework4.http.HttpResponse

class CombinedProcessor[A, B](processorA: GenericProcessor[A], processorB: GenericProcessor[B])
    extends GenericProcessor[(A, B)]:
  def apply[F[_]: Concurrent](url: String, response: HttpResponse): F[(A, B)] =
    Concurrent[F].parallelZip(
      processorA(url, response),
      processorB(url, response)
    )
