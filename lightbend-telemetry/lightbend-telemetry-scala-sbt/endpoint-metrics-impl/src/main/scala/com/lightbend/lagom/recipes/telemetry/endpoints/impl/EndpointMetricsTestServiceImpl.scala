package com.lightbend.lagom.recipes.telemetry.endpoints.impl

import java.util.UUID

import akka.{ Done, NotUsed }
import com.lightbend.lagom.recipes.telemetry.endpoints.api.{ EndpointMetricsTestService, Params }
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.{ ExceptionMessage, NotFound, TransportErrorCode, TransportException }

import scala.concurrent.Future
import scala.util.Random

class EndpointMetricsTestServiceImpl extends EndpointMetricsTestService {
  override def ok: ServiceCall[NotUsed, Done] =
    _ => Future.successful(Done)

  override def fail: ServiceCall[NotUsed, Done] =
    _ => Future.failed(createException)

  override def notFound: ServiceCall[NotUsed, Done] =
    _ => Future.failed(NotFound("not found"))

  override def maybeFail(probability: Double): ServiceCall[NotUsed, Done] =
    _ => if (Random.nextDouble() < probability)
           Future.failed(createException)
         else
           Future.successful(Done)

  private def createException = {
    new TransportException(
      TransportErrorCode.InternalServerError,
      new ExceptionMessage(getClass.getSimpleName, "failed")
    )
  }

  @volatile private var welcomeMessage: String = "Welcome"

  override def getWelcomeMessage: ServiceCall[NotUsed, String] =
    _ => Future.successful(welcomeMessage)

  override def putWelcomeMessage: ServiceCall[String, String] = { newWelcomeMessage =>
    welcomeMessage = newWelcomeMessage
    Future.successful(newWelcomeMessage)
  }


  override def staticPath: ServiceCall[NotUsed, Params] =
    _ => Future.successful(Params())

  override def pathParam(param: String): ServiceCall[NotUsed, Params] =
    _ => Future.successful(Params(param = Option(param)))

  override def multiSegmentPathParam(path: String): ServiceCall[NotUsed, Params] =
    _ => Future.successful(Params(path = Option(path)))

  override def regexParam(uuid: UUID): ServiceCall[NotUsed, Params] =
    _ => Future.successful(Params(uuid = Option(uuid)))

  override def queryParams(param: Option[String], names: Option[Seq[String]]): ServiceCall[NotUsed, Params] =
    _ => Future.successful(Params(param = param, names = names))

  override def allParams(
    param: String,
    uuid: UUID,
    path: String,
    names: Seq[String]
  ): ServiceCall[NotUsed, Params] =
    _ => Future.successful(Params(Option(param), Option(uuid), Option(path), Option(names)))
}
