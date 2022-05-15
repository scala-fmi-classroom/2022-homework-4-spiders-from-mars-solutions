package homework4.generic.processors

import homework4.generic.{Concurrent, GenericProcessor}
import homework4.http.HttpResponse
import homework4.processors.{WordCount, WordCounter}

object GenericWordCounter extends GenericProcessor[WordCount]:
  def apply[F[_]: Concurrent](url: String, response: HttpResponse): F[WordCount] = Concurrent[F].fromFuture {
    WordCounter(url, response)
  }
