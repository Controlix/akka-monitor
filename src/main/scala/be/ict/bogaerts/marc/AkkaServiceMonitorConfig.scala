package be.ict.bogaerts.marc

import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration.{Duration, FiniteDuration}

case class AkkaServiceMonitorConfig(
  interface: String,
  port: Int
)

object AkkaServiceMonitorConfig {

  def apply(): AkkaServiceMonitorConfig = apply(ConfigFactory.load.getConfig("akka-service-monitor-http"))

  def apply(config: Config): AkkaServiceMonitorConfig = {
    new AkkaServiceMonitorConfig(
      config.getString("interface"),
      config.getInt("port")
    )
  }
}