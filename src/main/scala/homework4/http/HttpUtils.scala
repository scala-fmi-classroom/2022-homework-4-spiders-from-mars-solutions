package homework4.http

import java.net.URI

import scala.util.Try

object HttpUtils:
  def isUrlHttp(url: String): Boolean =
    val maybeUri = Try(new URI(url)).toOption

    maybeUri.exists(uri => Set("http", "https")(uri.getScheme))

  def sameDomain(url1: String, url2: String): Boolean =
    val uri1 = new URI(url1)
    val uri2 = new URI(url2)

    uri1.getHost == uri2.getHost
