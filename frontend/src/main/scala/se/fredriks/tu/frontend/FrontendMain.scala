package se.fredriks.tu.frontend

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.twitter.finagle.http._
import com.twitter.finagle.http.path._
import com.twitter.finagle.{Http, Service}
import com.twitter.util._
import com.typesafe.config.ConfigFactory
import org.jboss.netty.buffer.{ChannelBufferOutputStream, ChannelBuffers}
import org.jboss.netty.handler.codec.http.HttpHeaders.Names
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType
import org.jboss.netty.handler.codec.http.multipart.{Attribute, HttpPostRequestDecoder}
import org.slf4j.LoggerFactory

object FrontendMain extends App {
  private val logger = LoggerFactory.getLogger(FrontendMain.getClass)

  val service = new Service[HttpRequest, HttpResponse] {
    def apply(req:HttpRequest) = {
      logger.debug(req.toString)
      try {
        req.getMethod -> Path(req.getUri) match {
          case HttpMethod.GET -> Root / "lookup" / tiny => lookup(req, tiny)
          case HttpMethod.POST -> Root / "create" => create(req)
        }
      }
      catch {
        case e:NoSuchElementException =>
          Future value Response(req.getProtocolVersion, HttpResponseStatus.NOT_FOUND)
        case e:MatchError =>
          Future value Response(req.getProtocolVersion, HttpResponseStatus.NOT_FOUND)
        case e:Exception =>
          logger.error("apply error", e)
          Future value Response(req.getProtocolVersion, HttpResponseStatus.INTERNAL_SERVER_ERROR)
      }
    }
  }

  val jsonMapper = new ObjectMapper()
  jsonMapper.registerModule(DefaultScalaModule)

  private def json(res:URLResult) = {
    val cb = ChannelBuffers.dynamicBuffer
    val out = new ChannelBufferOutputStream(cb)
    jsonMapper.writeValue(out, res)
    cb
  }

  private def toRedirectResponse(res:URLResult) : Future[HttpResponse] = res.url match {
    case None =>
      logger.debug("No URL found for " + res.tiny.get)
      Future value Response(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND)
    case Some(url) =>
      logger.debug("The URL for " + res.tiny.get + " is " + url)
      val r = Response(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND)
      r.headers().set(Names.LOCATION, url)
      Future value r
  }

  private def lookup(req:HttpRequest, tiny:String) : Future[HttpResponse] = {
    logger.debug("Lookup '" + tiny + "'")
    tinyURL.get(tiny) flatMap toRedirectResponse
  }

  private def create(req:HttpRequest) : Future[HttpResponse] = try {
    val post = new HttpPostRequestDecoder(req)

    val url = post.getBodyHttpData("URL")
    url.getHttpDataType match {
      case HttpDataType.Attribute =>
        val a = url.asInstanceOf[Attribute]
        logger.debug("Creating TinyURL for '"  + a.getValue + "'")
        tinyURL.set(a.getValue).flatMap({ res =>
          val resp = Response(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
          resp.setContentTypeJson()
          resp.setContent(json(res))
          Future value resp
        })
      case t =>
        logger.error("Unknown POST type" + t)
        Future value Response(req.getProtocolVersion, HttpResponseStatus.INTERNAL_SERVER_ERROR)
    }
  }
  catch {
    case e:Exception =>
      logger.error("Created failed with", e.getMessage)
      Future value Response(req.getProtocolVersion, HttpResponseStatus.INTERNAL_SERVER_ERROR)
  }

  // load config
  val conf = ConfigFactory.load()

  val tinyURL = new TinyURL(conf)

  val serverAddress = conf.getString("finagle_address")
  logger.debug("Starting HTTP server on " + serverAddress)
  val server = Http.serve(serverAddress, service)
  logger.debug("Server started")

  // Wait for keyboard to stop
  while(System.in.available() > 0) System.in.read()
  new Thread(new Runnable {
    override def run(): Unit = {
      println("Press any key")
      System.in.read()
      server.close(Time.fromMilliseconds(0))
    }
  }).start()
  Await.ready(server)
}
