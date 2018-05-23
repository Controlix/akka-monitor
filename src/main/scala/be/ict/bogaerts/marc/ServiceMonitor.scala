package be.ict.bogaerts.marc

import akka.actor.{ Actor, Props, ActorLogging }

object ServiceMonitor {
  def props(url: String): Props = Props(new ServiceMonitor(url))
  
  case object Start
  case object Stop
}

class ServiceMonitor(val url: String) extends Actor with ActorLogging {
  
  import ServiceMonitor._
  
  log.debug("New ServiceMonitor created")
  
  override def receive: Receive = {
    case Start => log.info("started")
    case Stop => log.info("stopped")
    case _ => log.info("something else")
  }
}