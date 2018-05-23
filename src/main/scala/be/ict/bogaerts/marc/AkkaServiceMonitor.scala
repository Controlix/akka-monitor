package be.ict.bogaerts.marc

import akka.actor.ActorSystem
import akka.actor.ActorLogging

object AkkaServiceMonitor extends App {

  import ServiceMonitor._

  val system: ActorSystem = ActorSystem("monitor")
  
  val serviceMonitor = system.actorOf(ServiceMonitor.props("http:google.com?q=akka"))

  serviceMonitor ! Start
  serviceMonitor ! Stop
  serviceMonitor ! Start
}
