package be.ict.bogaerts.marc

import akka.http.scaladsl.server.HttpApp
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.server.Directives._
import java.util.concurrent.TimeUnit
import akka.http.scaladsl.server.directives.DebuggingDirectives
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

case class StartMonitoring(method: String, url: String)
case class StopMonitoring(monitorName: String)

trait ServiceMonitorJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val startMonitoringFormat = jsonFormat2(StartMonitoring)
  implicit val stopMonitoringFormat = jsonFormat1(StopMonitoring)
}

object AkkaServiceMonitorHttp extends HttpApp with ServiceMonitorJsonSupport {

  import ServiceMonitor._

  val routes =
    pathPrefix("monitor") {
      get {
        log.info("Start monitoring google for 2 seconds ...")
        monitorGoogle
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>google was monitored</h1>"))
      } ~ post {
        path("start") {
          entity(as[StartMonitoring]) { startMonitoring =>
            log.info("got message {}", startMonitoring)
            val serviceMonitor = system.actorOf(ServiceMonitor.props(startMonitoring.method.toUpperCase(), startMonitoring.url))
            serviceMonitor ! Start
            complete(s"Started ${serviceMonitor.path.name}")
          }
        }
//        ~ path("stop") {
//          entity(as[StopMonitoring]) { stopMonitoring =>
//            log.info("got message {}", stopMonitoring)
//            val serviceMonitor = system.actorSelection(stopMonitoring.monitorName)
//            serviceMonitor ! Stop
//            complete("Stopped")
//          }
//        }
      }
    }

  private lazy val system = systemReference.get
  private lazy val log = system.log

  private lazy val monitorGoogle = {
    val serviceMonitor = system.actorOf(ServiceMonitor.props("GET", "http://google.com?q=akka"))

    serviceMonitor ! Start

    TimeUnit.SECONDS.sleep(2L)

    serviceMonitor ! Stop
  }
}