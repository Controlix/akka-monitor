package be.ict.bogaerts.marc

import java.io.{BufferedReader, InputStreamReader}
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Collectors

import akka.actor.{Actor, ActorLogging, Cancellable, Props}
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.{HttpUriRequest, RequestBuilder}
import org.apache.http.impl.client.{BasicResponseHandler, HttpClientBuilder}

import scala.concurrent.{Future, blocking}
import scala.concurrent.duration.DurationInt


object ServiceMonitor {
  def props(id: String): Props = Props(new ServiceMonitor(id))

  case object Init

  case object Start

  case object Stop

  case class HttpRequestProperties(url: String, httpMethod: String, body: String)

}

class ServiceMonitor(val id: String) extends Actor with ActorLogging {

  import spray.json._
  import DefaultJsonProtocol._
  import ServiceMonitor._

  implicit val httpRequestPropertiesFormat = jsonFormat3(HttpRequestProperties)

  import scala.concurrent.ExecutionContext.Implicits.global

  log.debug("New ServiceMonitor created")
  var scheduled: Cancellable = _
  val timeout = (5 seconds).toMillis.intValue()
  val config = RequestConfig.custom()
    .setConnectionRequestTimeout(timeout)
    .setConnectTimeout(timeout)
    .setSocketTimeout(timeout)
    .build()
  val httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build()

  val lastStatus = new AtomicInteger
  // Metrics.gauge("status.ok", Tags.of("method", method).and("url", url), lastStatus)
  var checkRequest: HttpUriRequest = _

  override def receive = uninitialized

  def uninitialized: Receive = {
    case Init => {
      log.info("got init")
      init
    }
    case _ => log.info("not yet initialized")
  }

  def stopped: Receive = {
    case Start => start
    case _ => log.info("already stopped")
  }

  def started: Receive = {
    case Stop => stop
    case Ping =>
      log.info("check service ...")
      val result = check
      log.info("Got status {}", result._1)
      result._1 match {
        case 200 => lastStatus.set(1)
        case _ => lastStatus.set(0)
      }
      log.info("Set last status to {}", lastStatus.get)
    case _ => log.info("already started")
  }

  def init = {
    log.info("init...")
    Future {
      blocking {
        val str = httpClient.execute(RequestBuilder.get(s"http://localhost:8090/monitor/$id/properties").build(), new BasicResponseHandler())
        log.info("Got {}", str)
        str
      }
    } foreach { response =>
      val properties = response.parseJson.convertTo[HttpRequestProperties]
      log.info("Got service properties {}", properties)
      checkRequest = RequestBuilder.create(properties.httpMethod).setUri(properties.url).build();

      context.become(stopped)
      self ! Start
    }
  }

  def stop = {
    log.info("stop...")
    scheduled.cancel()

    context.become(stopped)
    lastStatus.set(0)
    log.info("Set last status to {}", lastStatus.get)
    httpClient.close()
    context.stop(self)
  }

  def start = {
    log.info("start...")
    context.become(started)
    scheduled = context.system.scheduler.schedule(10 milliseconds, 1 minute, self, Ping)
  }

  def check = {
    try {
      val response = httpClient.execute(checkRequest)
      val entity = response.getEntity
      
      log.info("content is of type {} and length {}", entity.getContentType, entity.getContentLength)

      val inputStream = entity.getContent
      val result = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"))
      
      response.close()
      inputStream.close()
      
      log.debug("service status {}", response.getStatusLine)
      log.debug("result content = {}", result)
      
      (response.getStatusLine.getStatusCode, result)
    } catch {
      case _ => (-1, "")
    }
  }

  case object Ping

}