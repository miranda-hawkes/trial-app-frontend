/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package services

import connectors.PartialsConnector
import models.ServiceModel
import org.mockito.ArgumentMatchers
import org.scalatest.mock.MockitoSugar
import play.api.http.Status.{BAD_GATEWAY, INTERNAL_SERVER_ERROR, OK, UNAUTHORIZED}
import play.api.test.FakeRequest
import play.twirl.api.Html
import org.mockito.Mockito._
import traits.ControllerTestSpec
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.partials.HtmlPartial
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class PartialsServiceSpec extends UnitSpec with MockitoSugar with ControllerTestSpec {

  implicit val hc: HeaderCarrier = mock[HeaderCarrier]

  val service = ServiceModel("service name", "url")
  val mockPartialsConnector: PartialsConnector = mock[PartialsConnector]

  "PartialsService .handleFailure" when {

    val controller = new PartialsService(mockPartialsConnector, messagesApi)

    "status is 401" should {

      val result = controller.handleFailure(UNAUTHORIZED, service)

      "return Html" in {
        result.contentType shouldBe "text/html"
      }

      "have the text 'You are not authorised to view this content'" in {
        result.body should include("You are not authorised to view this content")
      }
    }

    "status is 500" should {

      val result = controller.handleFailure(INTERNAL_SERVER_ERROR, service)

      "return Html" in {
        result.contentType shouldBe "text/html"
      }

      "have the text '500 Please try again later.'" in {
        result.body should include("500 Please try again later.")
      }
    }

    "status is 502" should {

      val result = controller.handleFailure(BAD_GATEWAY, service)

      "return Html" in {
        result.contentType shouldBe "text/html"
      }

      "have the text '502 An unknown error has occurred'" in {
        result.body should include("502 An unknown error has occurred")
      }
    }
  }

  "PartialsService .handlePartial" when {

    val controller = new PartialsService(mockPartialsConnector, messagesApi)

    "partial retrieval is a Success" should {

      val partial = HtmlPartial.Success(None, Html("success"))
      val result = await(controller.handlePartial(partial, service))

      "return html body" in {
        result shouldEqual Html("success")
      }
    }

    "partial retrieval is a Failure" should {

      val partial = HtmlPartial.Failure(Some(500), "")
      val result = await(controller.handlePartial(partial, service))

      "return error text" in {
        result.body should include("500 Please try again later.")
      }
    }
  }

  "PartialsService .getPartials" should {

    when(mockPartialsConnector.getPartial(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(HtmlPartial.Success(None, Html("some content"))))
      .thenReturn(Future.successful(HtmlPartial.Success(None, Html("more content"))))

    val controller = new PartialsService(mockPartialsConnector, messagesApi)

    lazy val result = controller.getPartials(Seq(service, service))(FakeRequest())

    "return a sequence of html" in {
      result.map(r => await(r)) shouldEqual Seq(Html("some content"), Html("more content"))
    }
  }

  "PartialsService .postForm" should {

    when(mockPartialsConnector.postForm(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(HttpResponse(OK)))

    val controller = new PartialsService(mockPartialsConnector, messagesApi)
    lazy val result = controller.postForm(service)(FakeRequest())

    "return a HttpResponse" in {
      result.status shouldEqual OK
    }
  }
}
