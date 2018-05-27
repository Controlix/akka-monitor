package be.ict.bogaerts.marc

import akka.actor.{ Actor, Props, ActorLogging }
import scala.concurrent.duration._
import akka.actor.Cancellable
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.HttpHost
import org.apache.http.HttpRequest
import org.apache.http.client.methods.HttpGet


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
  
  override def receive = stopped
  
  def stopped: Receive = {
    case Start => start
    case _ => log.info("something else")
  }
  
  def started: Receive = {
    case Stop => stop
    case Ping => {
      log.info("check service ...")
      val httpClient = HttpClientBuilder.create().build()
      val response = httpClient.execute(new HttpGet(url))
      response.close()

      log.info("service status {}", response.getStatusLine)
    }
    case _ => log.info("something else")
  }

  def stop = {
    log.info("stopped")
    scheduled.cancel()
    context.become(stopped)
  }
  
  def start = {
    log.info("started")
    context.become(started)
    scheduled = context.system.scheduler.schedule(0 millisecond, 500 millisecond, self, Ping)
  }
  
  case object Ping
}