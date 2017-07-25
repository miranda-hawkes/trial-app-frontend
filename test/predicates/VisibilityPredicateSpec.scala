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

import models.AuthorisationDataModel
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.test.FakeRequest
import services.AuthorisationService
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.domain.{Accounts, Authority, ConfidenceLevel, CredentialStrength}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class VisibilityPredicateSpec extends UnitSpec with MockitoSugar with WithFakeApplication {

  def mockService(affinityGroup : String): AuthorisationService = {

    val mockService = mock[AuthorisationService]

    when(mockService.getAffinityGroup(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(affinityGroup)))

    mockService
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

  "Calling VisibilityPredicate" when {

    "user is an Organisation" should {

      val predicate = new VisibilityPredicate(mockService("Organisation"))
      val result = await(predicate(authContext, FakeRequest()))

      "return true" in {
        result.isVisible shouldEqual true
      }
    }

    "user is an Individual" should {

      val predicate = new VisibilityPredicate(mockService("Individual"))
      val result = await(predicate(authContext, FakeRequest()))

      "return false" in {
        result.isVisible shouldEqual false
      }
    }
  }

}
