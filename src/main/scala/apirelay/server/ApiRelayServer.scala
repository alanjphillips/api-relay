package apirelay.server


import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{FormData, HttpRequest}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import apirelay.config.ApplicationConfig.{InstagramConfig, TwitterConfig, UberConfig}
import apirelay.util.{ApiUtils, HttpUtils}
import com.typesafe.config.ConfigFactory
import spray.json.DefaultJsonProtocol

import scala.concurrent.Future


case class TwitterBearerToken(access_token: String)

object TokenJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val tokenFormats = jsonFormat1(TwitterBearerToken)
}

object ApiRelayServer extends App with HttpUtils with ApiUtils {
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)

  import TokenJsonSupport._

  def subscribeToInstagram(): Future[String] = {
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
          Future.failed(new Exception("Instagram call failed:\n " + entity))
        }
      }
    }
  }

  def getProductsFromUber(): Future[String] = {
    val connection = Http().outgoingConnectionTls(UberConfig.uberHost, UberConfig.uberPort)
    val request:HttpRequest = RequestBuilding.Get(UberConfig.productsUrl + toQueryString(uberGetProductsRequestParams))
    Source.single(request).via(connection).runWith(Sink.head).flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[String].flatMap { entity =>
          println("Uber Responded:\n " + entity)
          Future.successful(entity)
        }
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          Future.failed(new Exception("Uber call failed:\n " + entity))
        }
      }
    }
  }

  def getTwitterBearerTokenJson(): Future[String] = {
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
          Future.failed(new Exception("Twitter OAuth2 Bearer Token call failed:\n " + entity))
        }
      }
    }
  }

  def getTwitterBearerToken(): Future[TwitterBearerToken] = {
    val formFieldsAndValues = twitterGetOAuth2BearerTokenFormData
    val connection = Http().outgoingConnectionTls(TwitterConfig.twitterHost, TwitterConfig.twitterPort)
    val request:HttpRequest = RequestBuilding.Post(TwitterConfig.oauthTokenUrl, FormData(formFieldsAndValues)).withHeaders(twitterGetOAuth2BearerTokenHeaders)

    Source.single(request).via(connection).runWith(Sink.head).flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[TwitterBearerToken].flatMap { entity =>
          println("Twitter OAuth2 access_token:\n " + entity.access_token)
          Future.successful(entity)
        }
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          Future.failed(new Exception("Twitter OAuth2 Bearer Token call failed:\n " + entity))
        }
      }
    }
  }

  def getTwitterUserTimeline(user: String, token: TwitterBearerToken): Future[String] = {
    val connection = Http().outgoingConnectionTls(TwitterConfig.twitterHost, TwitterConfig.twitterPort)
    val request:HttpRequest = RequestBuilding.Get(TwitterConfig.userTimelineUrl + toQueryString(twitterGetUserTimelineRequestParams("20", user)))
      .withHeaders(twitterGetUserTimelineHeaders(token.access_token))

    Source.single(request).via(connection).runWith(Sink.head).flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[String].flatMap { entity =>
          println("Twitter User Timeline Responded:\n " + entity)
          Future.successful(entity)
        }
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
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
            getTwitterBearerTokenJson()
          }
        }
      } ~
      path("twitter" / "usertimeline" / Rest) { screenName =>
        get {
          complete {
            getTwitterBearerToken().flatMap {
              bearerToken => getTwitterUserTimeline(screenName, bearerToken)
            }
          }
        }
      }
  }

  def startServer() {
    Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
  }

  startServer()
}
