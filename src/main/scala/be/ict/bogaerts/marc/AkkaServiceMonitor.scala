package be.ict.bogaerts.marc

import akka.actor.ActorSystem
import akka.actor.ActorLogging
import java.util.concurrent.TimeUnit

object AkkaServiceMonitor extends App {

  import ServiceMonitor._

  val system: ActorSystem = ActorSystem("monitor")
  
  val serviceMonitor = system.actorOf(ServiceMonitor.props("http:google.com?q=akka"))

  serviceMonitor ! Start
  
  TimeUnit.SECONDS.sleep(30L)

  serviceMonitor ! Stop
}
