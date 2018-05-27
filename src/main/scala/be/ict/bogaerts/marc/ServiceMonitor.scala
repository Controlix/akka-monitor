package be.ict.bogaerts.marc

import akka.actor.{ Actor, Props, ActorLogging }
import scala.concurrent.duration._


object ServiceMonitor {
  def props(url: String): Props = Props(new ServiceMonitor(url))
  
  case object Start
  case object Stop
}

class ServiceMonitor(val url: String) extends Actor with ActorLogging {
  
  import ServiceMonitor._
  import scala.concurrent.ExecutionContext.Implicits.global
  
  log.debug("New ServiceMonitor created")
  
  override def receive = stopped
  
  def stopped: Receive = {
    case Start => start
    case _ => log.info("something else")
  }
  
  def started: Receive = {
    case Stop => stop
    case Ping => {
      log.info("check service ...")
      context.system.scheduler.scheduleOnce(500 millisecond, self, Ping)
    }
    case _ => log.info("something else")
  }

  def stop = {
    log.info("stopped")
    context.become(stopped)
  }
  
  def start = {
    log.info("started")
    context.become(started)
    self ! Ping
  }
  
  case object Ping
}