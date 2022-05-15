package homework4.generic

import homework4.SpideyConfig
import homework4.math.Monoid

class GenericSpidey[F[_]: Concurrent](httpClient: GenericHttpClient):
  def crawl[O: Monoid](startingUrl: String, config: SpideyConfig)(processor: GenericProcessor[O]): F[O] = ???
