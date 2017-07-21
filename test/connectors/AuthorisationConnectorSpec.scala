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

import models.AuthorisationDataModel
import org.mockito.ArgumentMatchers
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.mockito.Mockito.when
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.play.frontend.auth.connectors.domain.{ConfidenceLevel, CredentialStrength}

import scala.concurrent.Future

class AuthorisationConnectorSpec extends UnitSpec with MockitoSugar with WithFakeApplication {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "Authorisation Connector .getAuthResponse" should {

    lazy val mockHttp = mock[WSHttp]

    lazy val target = new AuthorisationConnector(mockHttp) {
      override lazy val serviceUrl: String = "localhost"
      override val authorityUri: String = "auth/authority"
    }

    val authResponse: JsObject = Json.obj(
      "uri" -> "/auth/oid/57e915480f00000f006d915b",
      "confidenceLevel" -> 200,
      "credentialStrength" -> "strong",
      "userDetailsLink" -> "http://localhost:9978/user-details/id/000000000000000000000000",
      "legacyOid" -> "00000000000000000000000",
      "new-session" -> "/auth/oid/57e915480f00000f006d915b/session",
      "ids" -> "/auth/oid/57e915480f00000f006d915b/ids",
      "credentials" -> Json.obj("gatewayId" -> "000000000000000"),
      "accounts" -> Json.obj("paye" -> Json.obj("link" -> "test", "nino" -> "AA123456A")),
      "lastUpdated" -> "2016-09-26T12:32:08.734Z",
      "loggedInAt" -> "2016-09-26T12 -> 32 -> 08.734Z",
      "levelOfAssurance" -> "1",
      "enrolments" -> "/auth/oid/00000000000000000000000/enrolments",
      "affinityGroup" -> "Individual",
      "correlationId" -> "0000000000000000000000000000000000000000000000000000000000000000",
      "credId" -> "000000000000000"
    )

    "with a valid request" should {

      when(mockHttp.GET[HttpResponse](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK, Some(authResponse))))

      val result = await(target.getAuthResponse())

      "return a model which" should {

        "have a confidence level of 200" in {
          result.confidenceLevel shouldBe ConfidenceLevel.L200
        }

        "have a credential strength of Strong" in {
          result.credentialStrength shouldBe CredentialStrength.Strong
        }

        "have a uri of /auth/oid/57e915480f00000f006d915b" in {
          result.uri shouldBe "/auth/oid/57e915480f00000f006d915b"
        }

        "have an Affinity Group of Individual" in {
          result.affinityGroup shouldBe "Individual"
        }
      }
    }

    "with an invalid request" should {

      when(mockHttp.GET[HttpResponse](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(HttpResponse(BAD_REQUEST, None)))

      "throw an exception" in {
        lazy val ex = intercept[Exception] {
          await(target.getAuthResponse())
        }

        ex.getMessage shouldEqual "Error returned from auth service"
      }
    }
  }
}
