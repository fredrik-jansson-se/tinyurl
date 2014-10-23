package se.fredriks.tu.frontend

import com.twitter.finagle.RedisClient
import com.twitter.finagle.redis.util.{CBToString, StringToChannelBuffer}
import com.twitter.util.Future
import com.typesafe.config.Config
import org.jboss.netty.buffer.ChannelBuffer
import org.slf4j.LoggerFactory

class MyRedisClient (conf:Config) {
  private val logger = LoggerFactory.getLogger(getClass)
  private val redis = RedisClient.newRichClient(conf.getString("redis_master"))

  private def handleLookup(res:Option[ChannelBuffer]) : Future[Option[String]] =
    Future.value(res.map(CBToString(_)))

  def getURL(tiny:String) : Future[Option[String]] = {
    val cb = StringToChannelBuffer(tiny)
    redis.get(StringToChannelBuffer(tiny)) flatMap handleLookup
  }

  def onError(e:Throwable): Unit = {
    logger.error(e.getMessage)
  }

  def setURL(tiny:String, url:String): Future[Unit] = {
    logger.debug("Storing " + tiny + " -> " + url)
    redis.set(StringToChannelBuffer(tiny), StringToChannelBuffer(url)).onFailure(onError)
  }

}
