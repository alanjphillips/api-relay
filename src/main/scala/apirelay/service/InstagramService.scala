package apirelay.service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{FormData, HttpRequest}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import apirelay.config.ApplicationConfig.InstagramConfig
import apirelay.util.ApiUtils

import scala.concurrent.{ExecutionContextExecutor, Future}


trait InstagramService extends ApiUtils {
  implicit def system: ActorSystem
  implicit def materializer: ActorMaterializer
  implicit def executor: ExecutionContextExecutor

  val instagramRoutes = {
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
      }
  }

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

}
