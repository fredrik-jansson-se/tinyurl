package se.fredriks.tu.frontend

import java.security.MessageDigest

import com.twitter.io.Charsets
import com.twitter.util.Future
import com.typesafe.config.Config
import org.slf4j.LoggerFactory

case class URLResult(url:Option[String], tiny:Option[String])

class TinyURL(conf:Config) {
  private val logger = LoggerFactory.getLogger(getClass)
  private val redis = new MyRedisClient(conf)
  private val minSize = 6 // Get from config

  private val md = MessageDigest.getInstance("SHA-256")

  private def createHash(url:String) = {
    val hash = md.digest(url.getBytes(Charsets.Utf8))
    val sb = new StringBuffer
    for (b <- hash) {
      sb.append(Integer.toHexString(b & 0xFF))
    }
    sb.toString
  }

  def set(url:String) : Future[URLResult] = {
    logger.debug("Shortening " + url)

    val hash = createHash(url).take(minSize)
    logger.debug("Hash is '" + hash + "'")

    redis.setURL(hash, url) flatMap {Unit =>
      Future value URLResult(Some(url), Some(hash))
    }
  }

  def get(tiny:String) : Future[URLResult] = {
    redis.getURL(tiny) flatMap(res => Future value URLResult(res, Some(tiny)))
  }
}
