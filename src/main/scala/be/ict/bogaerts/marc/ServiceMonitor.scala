package be.ict.bogaerts.marc

import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Collectors

import scala.concurrent.duration.DurationInt

import org.apache.http.client.methods.RequestBuilder
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Cancellable
import akka.actor.Props
import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.Tags
import org.apache.http.client.config.RequestConfig


object ServiceMonitor {
  def props(method:String, url: String): Props = Props(new ServiceMonitor(method, url))
  
  case object Start
  case object Stop
}

class ServiceMonitor(val method: String, val url: String) extends Actor with ActorLogging {
  
  import ServiceMonitor._
  import scala.concurrent.ExecutionContext.Implicits.global
  
  log.debug("New ServiceMonitor created")
  var scheduled: Cancellable = _
  var httpClient: CloseableHttpClient = _
  val timeout = (5 seconds).toMillis.intValue()
  val request = RequestBuilder.create(method).setUri(url).build()
  val config = RequestConfig.custom()
    .setConnectionRequestTimeout(timeout)
    .setConnectTimeout(timeout)
    .setSocketTimeout(timeout)
    .build()
  
  val lastStatus = new AtomicInteger
  Metrics.gauge("status.ok", Tags.of("method", method).and("url", url), lastStatus)
  
  override def receive = stopped
  
  def stopped: Receive = {
    case Start => start
    case _ => log.info("something else")
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
    case _ => log.info("something else")
  }

  def stop = {
    log.info("stopped")
    scheduled.cancel()
    
    context.become(stopped)
    lastStatus.set(0)
    log.info("Set last status to {}", lastStatus.get)
    httpClient.close()
    context.stop(self)
  }
  
  def start = {
    log.info("started")
    httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build()
    
    context.become(started)
    scheduled = context.system.scheduler.schedule(10 milliseconds, 1 minute, self, Ping)
  }
  
  def check = {
    try {
      val response = httpClient.execute(request)
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