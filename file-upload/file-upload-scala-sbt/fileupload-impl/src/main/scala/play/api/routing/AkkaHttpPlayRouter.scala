package play.api.routing

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.server.{Route, RoutingLog}
import akka.http.scaladsl.settings.{ParserSettings, RoutingSettings}
import akka.stream.{ActorMaterializerHelper, Materializer}
import play.api.mvc.akkahttp.AkkaHttpHandler
import play.api.routing.Router.Routes

import scala.concurrent.Future

object AkkaHttpPlayRouter {

  def apply(prefix: String, akkaHttpRoute: Route)(implicit actorSystem: ActorSystem, materializer: Materializer): Router = {
    val routingSettings = RoutingSettings(actorSystem)
    val routingLog = RoutingLog.fromActorSystem(actorSystem)
    val parserSettings = ParserSettings(actorSystem)
    apply(prefix, Route.asyncHandler(akkaHttpRoute)(routingSettings, parserSettings, materializer, routingLog))
  }


  def apply(prefix: String, handler: HttpRequest => Future[HttpResponse]): Router = {

    new SimpleRouter {
      val akkaHttpHandler = new AkkaHttpHandler {
        override def apply(request: HttpRequest): Future[HttpResponse] = handler(request)
      }
      override def routes: Routes = { case _ => akkaHttpHandler }
    }.withPrefix(prefix)
  }

}
