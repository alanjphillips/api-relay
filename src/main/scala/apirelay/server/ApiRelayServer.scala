package apirelay.server

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{FormData, HttpRequest}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import apirelay.config.ApplicationConfig.InstagramConfig
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future


object ApiRelayServer extends App {
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)

  def createInstagramSubscriptionFormData : Map[String,String] = {
    Map(
      "client_id" -> InstagramConfig.clientId,
      "client_secret" ->  InstagramConfig.clientSecret,
      "object" -> "user",
      "aspect" -> "media",
      "verify_token" -> "myVerifyToken",
      "callback_url" -> InstagramConfig.callbackUrl
    )
  }

  def subscribeToInstagram() = {
    val formFieldsAndValues = createInstagramSubscriptionFormData
    val connection = Http().outgoingConnectionTls(InstagramConfig.instagramHost, InstagramConfig.instagramPort)
    val request:HttpRequest = RequestBuilding.Post(InstagramConfig.subscriptionsUrl, FormData(formFieldsAndValues));

    Source.single(request).via(connection).runWith(Sink.head).flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[String].flatMap { entity =>
          println("Instagram Responded:\n " + entity)
          Future.successful(entity)
        }
        case _ => {
          println("POST to Instagram failed:\n " + response.toString)
          Future.failed(new Exception("Instagram call failed:\n " + response.toString))
        }
      }
    }
  }

  val routes = {
    pathPrefix("auth") {
      (get & path("instagram" / "callback")) { ctx =>
        println("Reached instagram callback: " + ctx.request.uri.query().get("hub.challenge") + "\n")
        ctx.complete(ctx.request.uri.query().get("hub.challenge"))
      }
    } ~
    path("instagram" / "subscription") {
      post {
        complete {
          subscribeToInstagram()
        }
      }
    }
  }

  def startServer() {
    Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
  }

  startServer()
}
