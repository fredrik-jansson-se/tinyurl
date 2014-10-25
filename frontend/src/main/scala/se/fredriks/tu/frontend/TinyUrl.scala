package se.fredriks.tu.frontend

import java.security.MessageDigest

import com.twitter.io.Charsets
import com.twitter.util.Future
import com.typesafe.config.Config
import org.slf4j.LoggerFactory

case class URLResult(url:Option[String], tiny:Option[String])

class TinyURL(conf:Config) {
  private val logger = LoggerFactory.getLogger(getClass)
  private val redis = new MyRedisConnections(conf)
  private val minSize = conf.getInt("hash_size")

  private val md = MessageDigest.getInstance("SHA-256")

  private def createHash(url:String) = {
    val hash = md.digest(url.getBytes(Charsets.Utf8))
    val sb = new StringBuffer
    for (b <- hash) {
      sb.append(Integer.toHexString(b & 0xFF))
    }
    sb.toString
  }

  def create(url:String) = {
    logger.debug("Shortening " + url)

    val hash = createHash(url).take(minSize)
    logger.debug("Hash is '" + hash + "'")

    redis.create(hash, url) flatMap {Unit =>
      Future value URLResult(Some(url), Some(hash))
    }
  }

  def lookup(tiny:String) = {
    redis.lookup(tiny) flatMap(res => Future value URLResult(res, Some(tiny)))
  }
}
