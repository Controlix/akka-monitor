package be.ict.bogaerts.marc

import akka.http.scaladsl.server.HttpApp
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.server.Directives._
import java.util.concurrent.TimeUnit
import akka.http.scaladsl.server.directives.DebuggingDirectives

object AkkaServiceMonitorHttp extends HttpApp with ServiceMonitorJsonSupport {

  import ServiceMonitor._

  val routes =
    path("monitor") {
      get {
        log.info("Start monitoring google ...")
        monitorGoogle
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>google was monitored</h1>"))
      } ~ post {
        entity(as[StartMonitoring]) { startMonitoring =>
          log.info("got message {}", startMonitoring)
          complete("ok")
        }
      }
    }
  
  private lazy val system = systemReference.get
  private lazy val log = system.log

  private lazy val monitorGoogle = {
    val serviceMonitor = system.actorOf(ServiceMonitor.props("http://google.com?q=akka"))

    serviceMonitor ! Start

    TimeUnit.SECONDS.sleep(5L)

    serviceMonitor ! Stop
  }
}