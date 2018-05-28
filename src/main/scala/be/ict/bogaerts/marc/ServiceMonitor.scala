package be.ict.bogaerts.marc

import akka.actor.{ Actor, Props, ActorLogging }
import scala.concurrent.duration._
import akka.actor.Cancellable
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.HttpHost
import org.apache.http.HttpRequest
import org.apache.http.client.methods.HttpGet
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.stream.Collectors
import org.apache.http.impl.client.CloseableHttpClient


object ServiceMonitor {
  def props(url: String): Props = Props(new ServiceMonitor(url))
  
  case object Start
  case object Stop
}

class ServiceMonitor(val url: String) extends Actor with ActorLogging {
  
  import ServiceMonitor._
  import scala.concurrent.ExecutionContext.Implicits.global
  
  log.debug("New ServiceMonitor created")
  var scheduled: Cancellable = _
  var httpClient: CloseableHttpClient = _
  
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
    case _ => log.info("something else")
  }

  def stop = {
    log.info("stopped")
    scheduled.cancel()
    
    context.become(stopped)
    httpClient.close()
  }
  
  def start = {
    log.info("started")
    httpClient = HttpClientBuilder.create().build()
    
    context.become(started)
    scheduled = context.system.scheduler.schedule(0 millisecond, 500 millisecond, self, Ping)
  }
  
  def check = {
      val response = httpClient.execute(new HttpGet(url))
      val entity = response.getEntity
      
      log.info("content is of type {} and length {}", entity.getContentType, entity.getContentLength)

      val inputStream = entity.getContent
      val result = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"))
      
      response.close()
      inputStream.close()
      
      log.debug("service status {}", response.getStatusLine)
      log.debug("result content = {}", result)
      
      (response.getStatusLine.getStatusCode, result)
  }
  
  case object Ping
}