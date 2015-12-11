package apirelay

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import apirelay.service.{InstagramService, TwitterService, UberService}
import com.typesafe.config.ConfigFactory


object ApiRelayServer extends App
  with TwitterService
  with UberService
  with InstagramService {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executor = system.dispatcher

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)

  val routes = {
    twitterRoutes ~
      uberRoutes ~
      instagramRoutes
  }

  def startServer() {
    Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
  }

  startServer()
}
