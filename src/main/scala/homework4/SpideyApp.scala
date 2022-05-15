package homework4

import homework4.http.AsyncHttpClient
import homework4.spiders.{BrokenLinkDetectorSpider, FileOutputSpider, ProcessingSpider, WordCounterSpider}

import java.util.concurrent.ForkJoinPool
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import scala.util.Try

case class SpideyAppInput(url: String, maxDepth: Int, spider: ProcessingSpider)

object NonNegativeInteger:
  def unapply(integerString: String): Option[Int] =
    Try(integerString.toInt).toOption.filter(_ >= 0)

object SpideyApp:
  given ExecutionContext = ExecutionContext.fromExecutor(new ForkJoinPool)
  val blockingExecutionContext: ExecutionContext = ExecutionContext.fromExecutor(new ForkJoinPool(4))

  val httpClient = new AsyncHttpClient
  val spidey = new Spidey(httpClient)

  val defaultConfig = SpideyConfig(
    maxDepth = 0,
    sameDomainOnly = false,
    tolerateErrors = true,
    retriesOnError = 0
  )

  val usage: String =
    """
      |Usage:
      |
      |SpideyApp <url> <max-depth> <processor> [processor-config]
      |
      |Possible processors and their config are:
      |
      |file-output <target-dir>
      |word-counter
      |broken-link-detector
      """.stripMargin

  def parseArguments(args: Array[String]): Option[SpideyAppInput] =
    def chooseSpider(processor: String, processorArgs: Seq[String]): Option[ProcessingSpider] = processor match
      case "file-output" =>
        processorArgs match
          case Seq(targetDir) => Some(new FileOutputSpider(targetDir)(blockingExecutionContext))
          case _ => None
      case "word-counter" => Some(WordCounterSpider)
      case "broken-link-detector" => Some(BrokenLinkDetectorSpider)
      case _ => None

    args match
      case Array(url, NonNegativeInteger(maxDepth), processor, processorArgs*) =>
        chooseSpider(processor, processorArgs)
          .map(SpideyAppInput(url, maxDepth, _))
      case _ => None

  def main(args: Array[String]): Unit =
    val maybeInput = parseArguments(args)

    maybeInput match
      case Some(SpideyAppInput(url, maxDepth, spider)) =>
        val eventualOutput = spider.process(spidey, url, maxDepth)(defaultConfig)

        val output = Await.result(eventualOutput, Duration.Inf)

        println("Spidey retrieved the following results:\n")
        println(output)
      case None =>
        if args.nonEmpty then println("Invalid arguments")
        println(usage)

    httpClient.shutdown()
