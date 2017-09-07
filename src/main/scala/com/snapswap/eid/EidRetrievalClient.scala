package com.snapswap.eid

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken, `Content-Type`}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.snapswap.eid.domain.EidVideoVerificationResult
import com.snapswap.eid.errors._
import spray.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

trait EidRetrievalClient {
  type VideoId = String

  def obtainVideoInfo(videoId: VideoId): Future[EidVideoVerificationResult]
}

class EidRetrievalAkkaHttpClient(token: String,
                                 apiHost: String,
                                 log: LoggingAdapter)
                                (implicit system: ActorSystem,
                                 ctx: ExecutionContext,
                                 materializer: Materializer) extends EidRetrievalClient {

  private val connection = Http().cachedHostConnectionPoolHttps[Unit](apiHost)

  private def videoInfoRequest(videoId: VideoId): HttpRequest =
    Get(s"/v2/videoid/$videoId").withHeaders(
      Authorization(OAuth2BearerToken(token)),
      `Content-Type`(ContentTypes.`application/json`)
    )

  private def send(request: HttpRequest): Future[HttpResponse] = {
    log.debug(s"Sending request $request")
    Source.single(request -> (()))
      .via(connection)
      .runWith(Sink.head)
      .map {
        case (Success(response), _) =>
          response
        case (Failure(ex), _) =>
          throw ex
      }
  }

  private def unmarshallToJson(response: HttpResponse): Future[JsValue] =
    if (response.status == StatusCodes.OK) {
      Unmarshal(response).to[String].map(_.parseJson).recoverWith {
        case NonFatal(ex) =>
          Future.failed(EidHttpResponseError(response.status, s"can't unmarshall into json due to ${exceptionDetails(ex)}"))
      }
    } else {
      Unmarshal(response).to[String].map { body =>
        throw EidHttpResponseError(response.status, s"status code wasn't OK, response body:\n${body.take(500)}\n...")
      }
    }


  override def obtainVideoInfo(videoId: VideoId): Future[EidVideoVerificationResult] = (for {
    _ <- Future(log.info(s"Retrieving video info vor videoId $videoId"))
    response <- send(videoInfoRequest(videoId)).recoverWith {
      case NonFatal(ex) =>
        Future.failed(EidHttpRequestError(exceptionDetails(ex)))
    }
    json <- unmarshallToJson(response)
    _ = log.debug(s"Video $videoId successfully unmarshalled into json:\n\n${json.prettyPrint}\n\n")
  } yield VideoInfoParser.parse(json)).recoverWith {
    case ex: EidException =>
      log.error(s"Handled exception during retrieving video info vor videoId $videoId was caught ${exceptionDetails(ex)}")
      Future.failed(ex)
    case NonFatal(ex) =>
      log.error(s"Unhandled exception during retrieving video info vor videoId $videoId was caught ${exceptionDetails(ex)}")
      Future.failed(EidMalformedResponse(exceptionDetails(ex)))
  }

}