package apirelay.service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{FormData, HttpRequest}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import apirelay.config.ApplicationConfig.TwitterConfig
import apirelay.util.ApiUtils
import spray.json.DefaultJsonProtocol

import scala.concurrent.{ExecutionContextExecutor, Future}


case class TwitterBearerToken(access_token: String)

object TokenJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val tokenFormats = jsonFormat1(TwitterBearerToken)
}

trait TwitterService extends ApiUtils {
  implicit def system: ActorSystem
  implicit def materializer: ActorMaterializer
  implicit def executor: ExecutionContextExecutor

  import TokenJsonSupport._

  val twitterRoutes = {
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

}
