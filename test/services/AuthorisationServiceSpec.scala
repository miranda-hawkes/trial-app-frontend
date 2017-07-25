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

import connectors.{AuthorisationConnector, FailedAuthResponse, SuccessAuthResponse}
import models.AuthorisationDataModel
import org.mockito.ArgumentMatchers
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.http.HeaderCarrier
import org.mockito.Mockito.when
import uk.gov.hmrc.play.frontend.auth.connectors.domain.{Accounts, ConfidenceLevel, CredentialStrength}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class AuthorisationServiceSpec extends UnitSpec with MockitoSugar {

  implicit val hc: HeaderCarrier = mock[HeaderCarrier]

  "Calling AuthorisationService .getAffinityGroup" should {

    "with a valid request" should {

      val authorisationService: AuthorisationService = {
        val authConnector = mock[AuthorisationConnector]
        val authorisationDataModel = AuthorisationDataModel(
          CredentialStrength.Strong,
          "Organisation",
          ConfidenceLevel.L200,
          "",
          Accounts()
        )

        when(authConnector.getAuthResponse()(ArgumentMatchers.any()))
          .thenReturn(Future.successful(SuccessAuthResponse(authorisationDataModel)))

        new AuthorisationService(authConnector)
      }

      val result = await(authorisationService.getAffinityGroup)

      "return an affinity group" in {
        result shouldEqual Some("Organisation")
      }
    }

    "with an invalid request" should {

      val authorisationService: AuthorisationService = {
        val authConnector = mock[AuthorisationConnector]

        when(authConnector.getAuthResponse()(ArgumentMatchers.any()))
          .thenReturn(Future.successful(FailedAuthResponse("error message")))

        new AuthorisationService(authConnector)
      }

      val result = await(authorisationService.getAffinityGroup)

      "return a None" in {
        result shouldEqual None
      }
    }
  }
}
