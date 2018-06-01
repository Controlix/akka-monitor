package be.ict.bogaerts.marc

import akka.actor.ActorSystem
import java.util.concurrent.TimeUnit

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}

import scala.util.{Failure, Success}
import akka.http.scaladsl.server.Directives._

import scala.concurrent.Await

object AkkaServiceMonitor extends App {

  // see https://github.com/theiterators/akka-http-microservice/blob/master/src/main/scala/AkkaHttpMicroservice.scala
  // see https://github.com/mkuthan/example-akka-http/blob/master/src/main/scala/example/ExampleAkkaHttpConf.scala

  import ServiceMonitor._

  implicit private val system: ActorSystem = ActorSystem("monitor")
  implicit private val executionContext = system.dispatcher
  private val config = AkkaServiceMonitorConfig()

  val route =
    path("hello") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
      }
    }

  private val serviceMonitor = system.actorOf(ServiceMonitor.props("http://google.com?q=akka"))

  serviceMonitor ! Start
  
  TimeUnit.SECONDS.sleep(30L)

  serviceMonitor ! Stop

  //logger.info(s"Starting server on ${config.interface}:${config.port}")
  Http().bindAndHandle(route, config.interface, config.port)
    .onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        //logger.info(s"Server is listening on ${address.getHostString}:${address.getPort}")
        registerShutdownHook(binding)
      case Failure(ex) =>
        //logger.error(s"Server could not be started", ex)
        stopAll()
    }

  private def registerShutdownHook(binding: Http.ServerBinding): Unit = {
    scala.sys.addShutdownHook {
      binding.unbind().onComplete { _ =>
        stopAll()
      }
    }
  }

  private def stopAll(): Unit = {
    system.terminate()
    Await.result(system.whenTerminated, config.shutdownTimeout)

  }

}
