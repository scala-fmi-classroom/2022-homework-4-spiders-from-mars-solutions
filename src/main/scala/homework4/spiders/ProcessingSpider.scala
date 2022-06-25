package homework4.spiders

import homework4.generic.{Concurrent, GenericProcessor, GenericSpidey}
import homework4.math.Monoid
import homework4.{Processor, Spidey, SpideyConfig}

import scala.concurrent.{ExecutionContext, Future}

trait ProcessingSpider:
  type Output

  def processor: GenericProcessor[Output]

  def monoid: Monoid[Output]

  def prettify(output: Output): String

  def process[F[_]: Concurrent](
    spidey: GenericSpidey[F],
    url: String,
    maxDepth: Int
  )(
    defaultConfig: SpideyConfig
  ): F[String] =
    spidey.crawl(url, defaultConfig.copy(maxDepth = maxDepth))(processor)(monoid).map(prettify)
