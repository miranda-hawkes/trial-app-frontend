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

package models

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import uk.gov.hmrc.play.frontend.auth.connectors.domain.{Accounts, ConfidenceLevel, CredentialStrength}

case class AuthorisationDataModel(credentialStrength: CredentialStrength,
                                  affinityGroup: String,
                                  confidenceLevel: ConfidenceLevel,
                                  uri: String,
                                  accounts: Accounts)

object AuthorisationDataModel {
  implicit val authReads: Reads[AuthorisationDataModel] = (
    (JsPath \ "credentialStrength").read[CredentialStrength] and
      (JsPath \ "affinityGroup").read[String] and
      (JsPath \ "confidenceLevel").read[ConfidenceLevel] and
      (JsPath \ "uri").read[String] and
      (JsPath \ "accounts").read[Accounts]
    )(AuthorisationDataModel.apply _)
}