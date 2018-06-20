package be.ict.bogaerts.marc

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.HttpApp
import be.ict.bogaerts.marc.AkkaServiceMonitorService._
import com.codahale.metrics.health.SharedHealthCheckRegistries
import spray.json.DefaultJsonProtocol

trait ServiceMonitorJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val startMonitoringFormat = jsonFormat1(StartMonitoring)
  implicit val stopMonitoringFormat = jsonFormat1(StopMonitoring)
  implicit val monitoringStartedFormat = jsonFormat1(MonitoringStarted)
  implicit val monitoringStoppedFormat = jsonFormat1(MonitoringStopped)
}

object AkkaServiceMonitorHttp extends HttpApp with ServiceMonitorJsonSupport {

  import scala.collection.JavaConverters._

  private val healthCheckRegistry = SharedHealthCheckRegistries.setDefault("healthchecks")

  //  new ClassLoaderMetrics().bindTo(prometheusRegistry)
  //  new JvmMemoryMetrics().bindTo(prometheusRegistry)
  //  new JvmGcMetrics().bindTo(prometheusRegistry)
  //  new ProcessorMetrics().bindTo(prometheusRegistry)
  //  new JvmThreadMetrics().bindTo(prometheusRegistry)

  private lazy val system = systemReference.get
  private lazy val log = system.log
  private lazy val service = AkkaServiceMonitorService(system)

  val routes =
    path("health") {
      get {
        complete(healthCheckRegistry.runHealthChecks().asScala.toMap.mapValues(result => result.toString))
      }
    } ~
      //    path("prometheus") {
      //      get {
      //        complete(prometheusRegistry.scrape())
      //      }
      //    } ~
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