package homework4.spiders

import homework4.math.Monoid
import homework4.{Processor, Spidey, SpideyConfig}

import scala.concurrent.{ExecutionContext, Future}

trait ProcessingSpider:
  type Output

  def processor: Processor[Output]

  def monoid: Monoid[Output]

  def prettify(output: Output): String

  def process(
    spidey: Spidey,
    url: String,
    maxDepth: Int
  )(
    defaultConfig: SpideyConfig
  )(using ec: ExecutionContext
  ): Future[String] =
    spidey.crawl(url, defaultConfig.copy(maxDepth = maxDepth))(processor)(monoid).map(prettify)
