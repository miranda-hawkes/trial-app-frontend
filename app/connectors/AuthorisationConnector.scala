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

import javax.inject.{Inject, Singleton}

import models.AuthorisationDataModel
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import config.WSHttp
import play.api.http.Status._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

sealed trait AuthResponse
case class SuccessAuthResponse(response: AuthorisationDataModel) extends AuthResponse
case class FailedAuthResponse(error: String) extends AuthResponse

@Singleton
class AuthorisationConnector @Inject()(http: WSHttp) extends ServicesConfig {

  lazy val serviceUrl: String = baseUrl("auth")
  val authorityUri: String = "auth/authority"

  def getAuthResponse()(implicit hc: HeaderCarrier): Future[AuthResponse] = {
    val url = s"$serviceUrl/$authorityUri"
    http.GET[HttpResponse](url).map {
      response =>
        response.status match {
          case OK =>
            SuccessAuthResponse(response.json.as[AuthorisationDataModel])
          case _ =>
            FailedAuthResponse(s"${response.status} returned from Auth with message: ${response.body}")
        }
    }
  }
}
