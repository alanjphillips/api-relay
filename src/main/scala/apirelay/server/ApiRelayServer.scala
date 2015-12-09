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
import apirelay.config.ApplicationConfig.{InstagramConfig, TwitterConfig, UberConfig}
import apirelay.util.{ApiUtils, HttpUtils}
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future


object ApiRelayServer extends App with HttpUtils with ApiUtils {
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)


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
        case  _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          println("POST to Instagram failed:\n " + entity)
          Future.failed(new Exception("Instagram call failed:\n " + entity))
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
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          println("GET to Uber failed:\n " + entity)
          Future.failed(new Exception("Uber call failed:\n " + entity))
        }
      }
    }
  }

  def getTwitterApplicationBearerToken() = {
    val formFieldsAndValues = twitterGetOAuth2BearerTokenFormData
    val connection = Http().outgoingConnectionTls(TwitterConfig.twitterHost, TwitterConfig.twitterPort)
    val request:HttpRequest = RequestBuilding.Post(TwitterConfig.oauthTokenUrl, FormData(formFieldsAndValues)).withHeaders(twitterGetOAuth2BearerTokenHeaders)

    Source.single(request).via(connection).runWith(Sink.head).flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[String].flatMap { entity =>
          println("Twitter OAuth2 Bearer Token Responded:\n " + entity)
          Future.successful(entity)
        }
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          println("POST to Twitter OAuth2 Bearer Token failed:\n " + entity)
          Future.failed(new Exception("Twitter OAuth2 Bearer Token call failed:\n " + entity))
        }
      }
    }
  }

  val routes = {
    pathPrefix("auth") {
      path("instagram" / "callback") {
        (get & parameters("hub.challenge")) {
          (hubChallenge) => {
            complete {
              hubChallenge
            }
          }
        }
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
      } ~
      path("twitter" / "apponlytoken") {
        get {
          complete {
            getTwitterApplicationBearerToken()
          }
        }
      }
  }

  def startServer() {
    Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
  }

  startServer()
}
