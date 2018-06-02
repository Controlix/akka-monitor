package be.ict.bogaerts.marc

import akka.http.scaladsl.server.HttpApp
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.server.Directives._
import java.util.concurrent.TimeUnit

object AkkaServiceMonitorHttp extends HttpApp {
  
  import ServiceMonitor._

  val routes =
    path("hello") {
      get {
        monitorGoogle
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Start monitoring google ...</h1>"))
      }
    }

  def monitorGoogle = {
    val serviceMonitor = systemReference.get.actorOf(ServiceMonitor.props("http://google.com?q=akka"))

    serviceMonitor ! Start

    TimeUnit.SECONDS.sleep(5L)

    serviceMonitor ! Stop
  }
}