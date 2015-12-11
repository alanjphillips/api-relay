package apirelay.service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import apirelay.config.ApplicationConfig.UberConfig
import apirelay.util.ApiUtils

import scala.concurrent.{ExecutionContextExecutor, Future}


trait UberService extends ApiUtils {
  implicit def system: ActorSystem
  implicit def materializer: ActorMaterializer
  implicit def executor: ExecutionContextExecutor

  val uberRoutes = {
    path("uber" / "products") {
      get {
        complete {
          getProductsFromUber()
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

}
