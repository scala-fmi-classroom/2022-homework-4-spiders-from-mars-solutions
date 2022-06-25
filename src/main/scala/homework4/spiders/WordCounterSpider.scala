package homework4.spiders

import homework4.generic.processors.GenericWordCounter
import homework4.math.Monoid
import homework4.processors.{WordCount, WordCounter}

object WordCounterSpider extends ProcessingSpider:
  type Output = WordCount

  def processor = GenericWordCounter

  def monoid: Monoid[WordCount] = summon

  def prettify(output: WordCount): String = output.wordToCount.toList
    .sortBy(_._2)(Ordering[Int].reverse)
    .map { case (word, count) =>
      s"$word was found $count times"
    }
    .mkString("\n")
