package se.fredriks.tu.frontend

import com.twitter.finagle.RedisClient
import com.twitter.finagle.redis.Client
import com.twitter.finagle.redis.util.{CBToString, StringToChannelBuffer}
import com.twitter.util.Future
import com.typesafe.config.Config
import org.jboss.netty.buffer.ChannelBuffer
import org.slf4j.LoggerFactory

class MyRedisConnections (conf:Config) {
  private val logger = LoggerFactory.getLogger(getClass)
  private val redisMaster = RedisClient.newRichClient(conf.getString("redis_master"))

  private class RedisInfo(val dest:String, val conn:Client)

  private val redisSlaves = conf.getString("redis_slave").split(';').map { dest =>
        new RedisInfo(dest, RedisClient.newRichClient(dest))
      }.toList

  private def toString(res:Option[ChannelBuffer]) = Future.value(res.map(CBToString(_)))

  private def lookup(tiny:String, slaves:List[RedisInfo]) : Future[Option[String]] = slaves match {
    case Nil =>
      throw new RuntimeException("No redis slaves available")
    case h::t =>
      logger.debug("Looking up " + tiny + " on " + h.dest)
      h.conn.get(StringToChannelBuffer(tiny)) flatMap toString rescue {
        case e:Throwable => lookup(tiny, t)
      }
  }

  def lookup(tiny:String) : Future[Option[String]] = {
    val shuffledClients = scala.util.Random.shuffle(redisSlaves.toList)
    lookup(tiny, shuffledClients)
  }

  def create(tiny:String, url:String) = {
    logger.debug("Storing " + tiny + " -> " + url)
    redisMaster.set(StringToChannelBuffer(tiny), StringToChannelBuffer(url))
  }

}
