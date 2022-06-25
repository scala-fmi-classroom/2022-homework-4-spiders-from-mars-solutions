package homework4.processors

import homework4.Processor
import homework4.html.HtmlUtils
import homework4.http.{ContentType, HttpResponse}
import homework4.math.Monoid
import homework4.math.Monoid.given

import scala.concurrent.Future

case class WordCount(wordToCount: Map[String, Int])

object WordCount:
  def wordsOf(text: String): List[String] = text.split("\\W+").toList.filter(_.nonEmpty)

  def empty = WordCount(Map.empty)

  given Monoid[WordCount] with
    extension (a: WordCount) def |+|(b: WordCount): WordCount = WordCount(a.wordToCount |+| b.wordToCount)

    def identity: WordCount = empty

object WordCounter extends Processor[WordCount]:
  def asText(response: HttpResponse): Option[String] = response.contentType.map(_.mimeType).collect {
    case ContentType.HtmlMimeType => HtmlUtils.toText(response.body)
    case ContentType.PlainTextMimeType => response.body
  }

  def countWords(text: String): WordCount =
    WordCount(
      WordCount
        .wordsOf(text)
        .groupBy(identity)
        .view
        .mapValues(_.size)
        .toMap
    )

  def apply(url: String, response: HttpResponse): Future[WordCount] = Future.successful {
    if response.isSuccess then asText(response).map(countWords).getOrElse(WordCount.empty)
    else WordCount.empty
  }
