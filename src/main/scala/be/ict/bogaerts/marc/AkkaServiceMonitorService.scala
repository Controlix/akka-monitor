package be.ict.bogaerts.marc

import akka.actor.ActorSystem

object AkkaServiceMonitorService {
  case class StartMonitoring(id: String)
  case class StopMonitoring(id: String)

  case class MonitoringStarted(id: String)
  case class MonitoringStopped(id: String)

  def apply(system: ActorSystem): AkkaServiceMonitorService = {
    new AkkaServiceMonitorService(system)
  }
}

class AkkaServiceMonitorService(val system: ActorSystem) {

  import AkkaServiceMonitorService._
  import ServiceMonitor._
  
  def startMonitor(startMonitoring: StartMonitoring): MonitoringStarted = {
    val serviceMonitor = system.actorOf(ServiceMonitor.props(startMonitoring.id))
    serviceMonitor ! Init
    
    MonitoringStarted(startMonitoring.id)
  }

  def stopMonitor(stopMonitoring: StopMonitoring): MonitoringStopped = {
    val serviceMonitor = system.actorSelection(s"user/${stopMonitoring.id}")
    serviceMonitor ! Stop
    
    MonitoringStopped(stopMonitoring.id)
  }
}