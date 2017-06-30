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

import java.util.UUID
import config.AppSessionCache
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import play.api.libs.json.Json
import traits.ControllerTestSpec
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.SessionId
import scala.concurrent.Future

class KeystoreConnectorSpec extends ControllerTestSpec {

  val sessionId: String = UUID.randomUUID.toString
  lazy implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(sessionId.toString)))
  val returnedCacheMap = CacheMap("key", Map("data" -> Json.toJson(Some("test"))))

  def setupTarget(): KeystoreConnector = {

    val subscriptionSessionCache: AppSessionCache = mock[AppSessionCache]

    when(subscriptionSessionCache.fetchAndGetEntry[String](ArgumentMatchers.eq("String"))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some("test")))

    when(subscriptionSessionCache.cache[String](ArgumentMatchers.anyString(), ArgumentMatchers.anyString())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(returnedCacheMap))

    new KeystoreConnector(mockConfig, subscriptionSessionCache)
  }

  "KeystoreConnector" when {

    lazy val target = setupTarget()

    "calling .fetchFormData" should {

      lazy val result = target.fetchAndGetFormData[String]("String")

      "return a string" in {
        await(result) shouldBe Some("test")
      }
    }

    "calling .saveFormData" should {

      lazy val result = target.saveFormData[String]("key", "test")

      "save data to keystore" in {
        await(result) shouldBe returnedCacheMap
      }
    }
  }
}
