package be.ict.bogaerts.marc

import akka.http.scaladsl.server.HttpApp
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.server.Directives._
import java.util.concurrent.TimeUnit
import akka.http.scaladsl.server.directives.DebuggingDirectives
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

import AkkaServiceMonitorService._
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.Metrics

trait ServiceMonitorJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val startMonitoringFormat = jsonFormat2(StartMonitoring)
  implicit val stopMonitoringFormat = jsonFormat1(StopMonitoring)
  implicit val monitoringStartedFormat = jsonFormat1(MonitoringStarted)
  implicit val monitoringStoppedFormat = jsonFormat1(MonitoringStopped)
}

object AkkaServiceMonitorHttp extends HttpApp with ServiceMonitorJsonSupport {

  private val prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
//  new ClassLoaderMetrics().bindTo(prometheusRegistry)
//  new JvmMemoryMetrics().bindTo(prometheusRegistry)
//  new JvmGcMetrics().bindTo(prometheusRegistry)
//  new ProcessorMetrics().bindTo(prometheusRegistry)
//  new JvmThreadMetrics().bindTo(prometheusRegistry)
  Metrics.addRegistry(prometheusRegistry)

  private lazy val system = systemReference.get
  private lazy val log = system.log
  private lazy val service = AkkaServiceMonitorService(system)

  val routes =
    path("prometheus") {
      get {
        complete(prometheusRegistry.scrape())
      }
    } ~
    pathPrefix("monitor") {
      post {
        path("start") {
          entity(as[StartMonitoring]) { startMonitoring =>
            log.info("got message {}", startMonitoring)
            complete(service.startMonitor(startMonitoring))
          }
        }
      } ~ post {
        path("stop") {
          entity(as[StopMonitoring]) { stopMonitoring =>
            log.info("got message {}", stopMonitoring)
            complete(service.stopMonitor(stopMonitoring))
          }
        }
      }
    }
}