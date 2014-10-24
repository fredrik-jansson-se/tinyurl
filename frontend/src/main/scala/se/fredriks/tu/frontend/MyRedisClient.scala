package se.fredriks.tu.frontend

import com.twitter.finagle.RedisClient
import com.twitter.finagle.redis.util.{CBToString, StringToChannelBuffer}
import com.twitter.util.Future
import com.typesafe.config.Config
import org.jboss.netty.buffer.ChannelBuffer
import org.slf4j.LoggerFactory

import scala.util.Random

class MyRedisClient (conf:Config) {
  private val logger = LoggerFactory.getLogger(getClass)
  private val redisMaster = RedisClient.newRichClient(conf.getString("redis_master"))

  private val redisSlaveCS = conf.getString("redis_slave").split(';')

  private val redisSlaves = redisSlaveCS.map(RedisClient.newRichClient(_))

  private val rand = new Random(System.currentTimeMillis())

  private def handleLookup(res:Option[ChannelBuffer]) : Future[Option[String]] =
    Future.value(res.map(CBToString(_)))

  def getURL(tiny:String) : Future[Option[String]] = {
    val cb = StringToChannelBuffer(tiny)
    val idx = rand.nextInt(redisSlaves.length)
    logger.debug("Looking up " + tiny + " on " + redisSlaveCS(idx))
    redisSlaves(idx).get(StringToChannelBuffer(tiny)) flatMap handleLookup
  }

  def onError(e:Throwable): Unit = {
    logger.error(e.getMessage)
  }

  def setURL(tiny:String, url:String): Future[Unit] = {
    logger.debug("Storing " + tiny + " -> " + url)
    redisMaster.set(StringToChannelBuffer(tiny), StringToChannelBuffer(url)).onFailure(onError)
  }

}
