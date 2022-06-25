package homework4.spiders

import homework4.generic.processors.GenericBrokenLinkDetector
import homework4.math.Monoid
import homework4.math.Monoid.given
import homework4.processors.BrokenLinkDetector

object BrokenLinkDetectorSpider extends ProcessingSpider:
  type Output = Set[String]

  def processor = GenericBrokenLinkDetector

  def monoid: Monoid[Set[String]] = summon

  def prettify(output: Set[String]): String = output.mkString("\n")
