package homework4.html

import org.jsoup.Jsoup
import org.jsoup.select.Elements

import scala.jdk.CollectionConverters.*

object HtmlUtils:
  def linksOf(html: String, url: String): List[String] =
    val document = Jsoup.parse(html, url)

    val anchors = document.select("a[href]")
    val imports = document.select("link[href]")
    val media = document.select("[src]")

    def retrieveUrl(elements: Elements, urlAttribute: String) =
      elements.iterator.asScala.map(_.absUrl(urlAttribute)).toList

    val links = retrieveUrl(anchors, "href") :::
      retrieveUrl(imports, "href") :::
      retrieveUrl(media, "src")

    links.distinct

  def toText(html: String): String = Jsoup.parse(html).text()
