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

package predicates

import connectors.{AuthResponse, AuthorisationConnector, SuccessAuthResponse}
import models.AuthorisationDataModel
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.test.FakeRequest
import services.AuthorisationService
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.domain.{Accounts, Authority, ConfidenceLevel, CredentialStrength}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class AffinityGroupPredicateSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  "Instantiating AffinityGroupPredicate" when {

    implicit val hc: HeaderCarrier = mock[HeaderCarrier]

    def mockService(response: AuthResponse): AuthorisationService = {

      val mockAuthConnector: AuthorisationConnector = mock[AuthorisationConnector]

      when(mockAuthConnector.getAuthResponse()(ArgumentMatchers.any()))
        .thenReturn(Future.successful(response))

      new AuthorisationService(mockAuthConnector)
    }

    val authContext = AuthContext(
      Authority(
        "userId",
        Accounts(),
        None,
        None,
        CredentialStrength.Strong,
        ConfidenceLevel.L200,
        None,
        None,
        None,
        ""
      )
    )

    "supplied with valid organisation credentials" should {

      val authorisationDataModel = AuthorisationDataModel(
        CredentialStrength.Strong,
        "Organisation",
        ConfidenceLevel.L200,
        "",
        Accounts()
      )

      val authService: AuthorisationService = mockService(SuccessAuthResponse(authorisationDataModel))
      val predicate = new AffinityGroupPredicate(authService)

      val result = await(predicate(authContext, FakeRequest()))

      "return true" in {
        result.isVisible shouldEqual true
      }
    }

    "supplied with invalid credentials" should {

      val authorisationDataModel = AuthorisationDataModel(
        CredentialStrength.Strong,
        "Individual",
        ConfidenceLevel.L200,
        "",
        Accounts()
      )

      val authService: AuthorisationService = mockService(SuccessAuthResponse(authorisationDataModel))
      val predicate = new AffinityGroupPredicate(authService)

      val result = await(predicate(authContext, FakeRequest()))

      "return false" in {
        result.isVisible shouldEqual false
      }
    }
  }
}
