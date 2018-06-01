package be.ict.bogaerts.marc

import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration.FiniteDuration

case class AkkaServiceMonitorConfig(
  interface: String,
  port: Int,
  shutdownTimeout: FiniteDuration,
  helloServiceInstances: Int
)

object AkkaServiceMonitorConfig {

  def apply(): AkkaServiceMonitorConfig = apply(ConfigFactory.load.getConfig("service-monitor-akka-http"))

  def apply(config: Config): AkkaServiceMonitorConfig = {
    new AkkaServiceMonitorConfig(
      config.as[String]("interface"),
      config.as[Int]("port"),
      config.as[FiniteDuration]("shutdown-timeout"),
      config.as[Int]("hello-service-instances")
    )
  }
}