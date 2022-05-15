package homework4.processors

import homework4.Processor
import homework4.http.HttpResponse
import homework4.math.Monoid

import scala.concurrent.Future

case class WordCount(wordToCount: Map[String, Int])

object WordCount:
  def wordsOf(text: String): List[String] = text.split("\\W+").toList.filter(_.nonEmpty)

  given Monoid[WordCount] = ???

object WordCounter extends Processor[WordCount]:
  def apply(url: String, response: HttpResponse): Future[WordCount] = ???
