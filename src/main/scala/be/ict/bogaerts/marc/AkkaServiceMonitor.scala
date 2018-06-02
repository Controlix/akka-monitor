package be.ict.bogaerts.marc

import java.util.concurrent.TimeUnit
import akka.actor.ActorSystem

object AkkaServiceMonitor extends App {

  private val system: ActorSystem = ActorSystem("service-monitor")
  private val config = AkkaServiceMonitorConfig()
  
  AkkaServiceMonitorHttp.startServer(config.interface, config.port, system)
  
  system.terminate()
}
