package com.lightbend.lagom.recipes.telemetry.endpoints.impl

import java.util.UUID

import akka.Done
import com.lightbend.lagom.recipes.telemetry.endpoints.api.{ EndpointMetricsTestService, Params }
import com.lightbend.lagom.scaladsl.api.transport.{ NotFound, TransportException }
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, Matchers }

import scala.concurrent.Future

class EndpointMetricsTestServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(ServiceTest.defaultSetup) { ctx =>
    new EndpointMetricsTestApplication(ctx) with LocalServiceLocator
  }

  private val client = server.serviceClient.implement[EndpointMetricsTestService]

  override protected def afterAll(): Unit = server.stop()

  "ok" should {
    "return Done" in {
      for (result <- client.ok.invoke())
        yield result shouldBe Done
    }
  }

  "fail" should {
    "throw an exception" in {
      recoverToSucceededIf[TransportException] {
        client.fail.invoke()
      }
    }
  }

  "notFound" should {
    "throw a NotFound exception" in {
      recoverToSucceededIf[NotFound] {
        client.notFound.invoke()
      }
    }
  }

  "maybeFail" when {
    "probability = 0.0" should {
      "always succeed" in {
        val requests = 1 until 1000 map { _ => client.maybeFail(0).invoke() }
        Future.sequence(requests).map { results =>
          all(results) shouldBe Done
        }
      }
    }
    "probability = 1.0" should {
      "always fail" in {
        val requests = 1 until 1000 map { _ => client.maybeFail(1).invoke().failed }
        Future.sequence(requests).map { results =>
          all(results) shouldBe an[Exception]
        }
      }
    }
  }

  "getWelcomeMessage" should {
    "return 'Welcome' by default" in {
      for (result <- client.getWelcomeMessage.invoke())
        yield result shouldEqual "Welcome"
    }
  }

  "putWelcomeMessage" should {
    "change the result of getWelcomeMessage" in {
      for {
        _ <- client.putWelcomeMessage.invoke("Willkommen")
        result <- client.getWelcomeMessage.invoke()
      } yield result shouldEqual "Willkommen"
    }
  }

  "staticPath" should {
    "return an empty Params" in {
      for (result <- client.staticPath.invoke())
        yield result shouldEqual Params()
    }
  }

  "pathParam" should {
    "return Params with a param field" in {
      for (result <- client.pathParam("probeParam").invoke())
        yield result shouldEqual Params(param = Some("probeParam"))
    }
  }

  "multiSegmentPathParam" should {
    "return Params with a path field" in {
      for (result <- client.multiSegmentPathParam("/probe/path/param").invoke())
        yield result shouldEqual Params(path = Some("/probe/path/param"))
    }
  }

  "regexParam" should {
    "return Params with a uuid field" in {
      val uuid = UUID.randomUUID()
      for (result <- client.regexParam(uuid).invoke())
        yield result shouldEqual Params(uuid = Some(uuid))
    }
  }

  "queryParams" should {
    "return Params with a param and names field" in {
      for (result <- client.queryParams(Some("probeParam"), Some(Seq("Alice", "Bob"))).invoke())
        yield result shouldEqual Params(param = Some("probeParam"), names = Some(Seq("Alice", "Bob")))
    }
  }

  "allParams" should {
    "return Params with all fields" in {
      val uuid = UUID.randomUUID()
      for (result <- client.allParams("probeParam", uuid, "/probe/path/param", Seq("Alice", "Bob")).invoke())
        yield result shouldEqual Params(
          param = Some("probeParam"),
          uuid = Some(uuid),
          path = Some("/probe/path/param"),
          names = Some(Seq("Alice", "Bob"))
        )
    }
  }
}
