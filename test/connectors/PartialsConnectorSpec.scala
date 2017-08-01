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

package connectors

import config.WSHttp
import models.ServiceModel
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.http.Status.{OK, UNAUTHORIZED}
import play.api.test.FakeRequest
import play.twirl.api.Html
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.partials.HtmlPartial
import uk.gov.hmrc.play.test.UnitSpec
import play.api.http.Status._
import scala.concurrent.Future

class PartialsConnectorSpec extends UnitSpec with MockitoSugar {

  implicit val hc: HeaderCarrier = mock[HeaderCarrier]

  def setup(response: HttpResponse): PartialsConnector = {
    val mockWSHttp: WSHttp = mock[WSHttp]

    when(mockWSHttp.GET[HttpResponse](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(response))

    when(mockWSHttp.POSTForm[HttpResponse](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(response))

    new PartialsConnector(mockWSHttp)
  }

  val service = ServiceModel("service name", "url")

  "PartialsConnector .getPartial" when {

    "200 is returned" should {

      val controller = setup(HttpResponse(OK, responseString = Some("some content")))

      lazy val result = controller.getPartial(service)(FakeRequest())

      "return HtmlPartial Success with content" in {
        await(result) shouldEqual HtmlPartial.Success(None, Html("some content"))
      }
    }

    "401 is returned" should {

      val controller = setup(HttpResponse(UNAUTHORIZED, responseString = Some("unauthorised")))

      lazy val result = controller.getPartial(service)(FakeRequest())

      "return HtmlPartial Failure with content" in {
        await(result) shouldEqual HtmlPartial.Failure(Some(UNAUTHORIZED), "unauthorised")
      }
    }
  }

  "PartialsConnector .postForm" when {

    "a valid form is submitted and 200 is returned" should {

      val response = HttpResponse(OK)
      val controller = setup(response)
      val request = FakeRequest().withFormUrlEncodedBody("id" -> "1")
      lazy val result = controller.postForm(service)(request)

      "return a 200" in {
        await(result.status) shouldEqual OK
      }
    }

    "no form is submitted" should {

      val controller = setup(HttpResponse(OK))
      val request = FakeRequest()
      lazy val result = controller.postForm(service)(request)

      "return a 500" in {
        await(result.status) shouldEqual INTERNAL_SERVER_ERROR
      }
    }
  }
}
