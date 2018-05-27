package be.ict.bogaerts.marc

import akka.actor.{ Actor, Props, ActorLogging }
import scala.concurrent.duration._
import akka.actor.Cancellable


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