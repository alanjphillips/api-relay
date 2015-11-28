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
import apirelay.config.ApplicationConfig.{InstagramConfig, UberConfig}
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future


object ApiRelayServer extends App {
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)

  def toQueryString(queryMap: Map[String,String]) = "?"+queryMap.map{
    case (key,value) => s"$key=$value" // What about uri encoding??
  }.mkString("&")

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

  def uberGetProductsRequestParams : Map[String,String] = {
    Map(
      "server_token" -> UberConfig.serverToken,
      "latitude" -> "51.531679",
      "longitude" -> "-0.124400"
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

  def getProductsFromUber() = {
    val connection = Http().outgoingConnectionTls(UberConfig.uberHost, UberConfig.uberPort)
    val request:HttpRequest = RequestBuilding.Get(UberConfig.productsUrl + toQueryString(uberGetProductsRequestParams))
    Source.single(request).via(connection).runWith(Sink.head).flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[String].flatMap { entity =>
          println("Uber Responded:\n " + entity)
          Future.successful(entity)
        }
        case _ => {
          println("GET to Uber failed:\n " + response.toString)
          Future.failed(new Exception("Uber call failed:\n " + response.toString))
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
    } ~
    path("uber" / "products") {
      get {
        complete {
          getProductsFromUber()
        }
      }
    }
  }

  def startServer() {
    Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
  }

  startServer()
}
