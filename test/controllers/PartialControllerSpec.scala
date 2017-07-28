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

package controllers

import javax.inject.Inject

import config.WSHttp
import org.mockito.ArgumentMatchers
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.test.UnitSpec
import play.api.http.Status._
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import play.api.test.FakeRequest
import play.twirl.api.Html
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.partials.HtmlPartial

import scala.concurrent.Future

class PartialControllerSpec extends UnitSpec with MockitoSugar {

  implicit val hc: HeaderCarrier = mock[HeaderCarrier]

  class TestController @Inject()(http: WSHttp = mock[WSHttp]) extends FrontendController with PartialController {
    override val urls: Seq[String] = Seq("", "")
    override val wsHttp: WSHttp = http
  }

  def setup(response: HttpResponse): TestController = {
    val mockWSHttp: WSHttp = mock[WSHttp]

    when(mockWSHttp.GET[HttpResponse](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(response))

    new TestController(mockWSHttp)
  }

  "PartialController .handleFailure" when {

    val controller = new TestController

    "status is 401" should {

      val result = controller.handleFailure(UNAUTHORIZED)

      "return Html" in {
        result.contentType shouldBe "text/html"
      }

      "have the text '401 Error: Confidence level not 200'" in {
        result.body should include("401 Error: Confidence level not 200")
      }
    }

    "status is 500" should {

      val result = controller.handleFailure(INTERNAL_SERVER_ERROR)

      "return Html" in {
        result.contentType shouldBe "text/html"
      }

      "have the text '500 Please try again later.'" in {
        result.body should include("500 Please try again later.")
      }
    }

    "status is 502" should {

      val result = controller.handleFailure(BAD_GATEWAY)

      "return Html" in {
        result.contentType shouldBe "text/html"
      }

      "have the text '502 An unknown error has occurred'" in {
        result.body should include("502 An unknown error has occurred")
      }
    }
  }

  "PartialController .handlePartial" when {

    val controller = new TestController

    "partial retrieval is a Success" should {

      val partial = HtmlPartial.Success(None, Html("success"))
      val result = await(controller.handlePartial(partial))

      "return html body" in {
        result shouldEqual Html("success")
      }
    }

    "partial retrieval is a Failure" should {

      val partial = HtmlPartial.Failure(Some(500), "")
      val result = await(controller.handlePartial(partial))

      "return error text" in {
        result.body should include("500 Please try again later.")
      }
    }
  }

  "PartialController .getPartial" when {

    "200 is returned" should {

      val controller = setup(HttpResponse(OK, responseString = Some("some content")))

      lazy val result = controller.getPartial("url")(FakeRequest())

      "return HtmlPartial Success with content" in {
        await(result) shouldEqual HtmlPartial.Success(None, Html("some content"))
      }
    }

    "401 is returned" should {

      val controller = setup(HttpResponse(UNAUTHORIZED, responseString = Some("unauthorised")))

      lazy val result = controller.getPartial("url")(FakeRequest())

      "return HtmlPartial Failure with content" in {
        await(result) shouldEqual HtmlPartial.Failure(Some(UNAUTHORIZED), "unauthorised")
      }
    }
  }

  "PartialController .getPartials" should {

    val controller = setup(HttpResponse(OK, responseString = Some("some content")))

    lazy val result = controller.getPartials(FakeRequest())

    "return a sequence of html" in {
      result.map(r => await(r)) shouldEqual Seq(Html("some content"), Html("some content"))
    }
  }
}
