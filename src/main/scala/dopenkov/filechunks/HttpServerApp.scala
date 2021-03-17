package dopenkov.filechunks

import akka.actor.CoordinatedShutdown
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}


object HttpServerApp {

  def main(args: Array[String]): Unit = {
    val logger = LoggerFactory.getILoggerFactory.getLogger("server")

    val applicationConfig = ConfigFactory.load("application")
    val defaultConfig = applicationConfig.getConfig("default_config")
    val config = applicationConfig.getConfig("config").withFallback(defaultConfig)

    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "Hashes")

    implicit val executionContext: ExecutionContextExecutor = system.executionContext
    val route = StreamingHashes.route(256 * 1024) ~ StreamingHashes.testRoute

    val bindAddress = "0.0.0.0"
    val port = config.getInt("port")
    val bindingFuture = Http().newServerAt(bindAddress, port).bind(route)


    bindingFuture.onComplete {
      case Failure(exception) =>
        logger.error("Cannot start server", exception)
        system.terminate()
      case Success(binding) =>
        val cs: CoordinatedShutdown = CoordinatedShutdown(system)
        cs.addTask(CoordinatedShutdown.PhaseServiceStop, "unbind-server") { () =>
          println("Exiting...")
          binding.unbind()
        }
        println(s"Server online at http://$bindAddress:$port/\nPress Ctrl-C to stop...")
    }
  }
}