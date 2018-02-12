package com.lightbend.lagom.recipes.telemetry.endpoints.api

import java.util.UUID

import akka.{ Done, NotUsed }
import com.lightbend.lagom.scaladsl.api.Service._
import com.lightbend.lagom.scaladsl.api.transport.{ HeaderFilter, Method, RequestHeader, ResponseHeader }
import com.lightbend.lagom.scaladsl.api.{ CircuitBreaker, Descriptor, Service, ServiceCall }
import play.api.libs.json.{ Format, Json }

trait EndpointMetricsTestService extends Service {

  /* Always returns 200 OK */
  def ok: ServiceCall[NotUsed, Done]

  /* Always returns 500 Internal Server Error */
  def fail: ServiceCall[NotUsed, Done]

  /* Always returns 404 Not Found */
  def notFound: ServiceCall[NotUsed, Done]

  /* Takes a probability of failure */
  def maybeFail(probability: Double): ServiceCall[NotUsed, Done]


  /* GET/PUT with the same URI path */

  def getWelcomeMessage: ServiceCall[NotUsed, String]

  def putWelcomeMessage: ServiceCall[String, String]


  /* Various path expressions */

  def staticPath: ServiceCall[NotUsed, Params]

  def pathParam(param: String): ServiceCall[NotUsed, Params]

  def multiSegmentPathParam(path: String): ServiceCall[NotUsed, Params]

  def regexParam(uuid: UUID): ServiceCall[NotUsed, Params]

  def queryParams(param: Option[String], names: Option[Seq[String]]): ServiceCall[NotUsed, Params]

  def allParams(param: String, uuid: UUID, path: String, names: Seq[String]): ServiceCall[NotUsed, Params]


  override def descriptor: Descriptor =
    named("endpoint-metrics")
      .withCalls(
        // "call" uses the method name to define the path
        call(ok),
        call(fail)
          // Use a custom circuit breaker to avoid affecting other calls
          .withCircuitBreaker(CircuitBreaker.identifiedBy("fail")),

        // "namedCall" takes an alternate name
        namedCall("not-found", notFound)
          // Use a custom circuit breaker to avoid affecting other calls
          .withCircuitBreaker(CircuitBreaker.identifiedBy("not-found")),

        // "pathCall" allows parameters
        pathCall("/maybe-fail?probability", maybeFail _)
          // Use a custom circuit breaker to avoid affecting other calls
          .withCircuitBreaker(CircuitBreaker.identifiedBy("maybe-fail")),

        // "restCall" also specifies the HTTP method
        restCall(Method.GET, "/welcome-message", getWelcomeMessage),
        restCall(Method.PUT, "/welcome-message", putWelcomeMessage),

        // "pathCall" supports a few types of path expressions
        pathCall("/static/path", staticPath),
        pathCall("/path/:param", pathParam _),
        pathCall("/multi/segment/*path", multiSegmentPathParam _),
        pathCall(
          "/regex/$uuid<[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}>",
          regexParam _
        ),
        pathCall("/query?param&names", queryParams _),
        pathCall(
          "/all/:param/$uuid<[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}>" +
          "/*path?names",
          allParams _
        )
      )
      .withAutoAcl(true)

}

case class Params(
  param: Option[String] = None,
  uuid: Option[UUID] = None,
  path: Option[String] = None,
  names: Option[Seq[String]] = None
)

object Params {
  implicit val format: Format[Params] = Json.format
}
