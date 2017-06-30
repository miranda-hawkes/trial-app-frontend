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

package traits

import akka.stream.Materializer
import config.AppConfig
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import play.api.i18n.MessagesApi
import uk.gov.hmrc.play.test.UnitSpec


trait ControllerTestSpec extends UnitSpec with MockitoSugar with OneAppPerSuite {

  val mockConfig: AppConfig = app.injector.instanceOf[AppConfig]
  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  implicit val mat: Materializer = app.injector.instanceOf[Materializer]

}
