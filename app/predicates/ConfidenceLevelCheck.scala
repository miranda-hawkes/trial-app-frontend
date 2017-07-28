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
import uk.gov.hmrc.play.frontend.auth.connectors.domain.ConfidenceLevel

import scala.concurrent.Future

object ConfidenceLevelCheck {

  def confidenceLevelCheck(authModel: Option[AuthorisationDataModel]): Future[Boolean] =
    authModel match {
      case Some(model) => model.confidenceLevel match {
        case ConfidenceLevel.L200 => Future.successful(true)
        case ConfidenceLevel.L300 => Future.successful(true)
        case ConfidenceLevel.L500 => Future.successful(true)
        case _ => Future.successful(false)
      }
      case None => Future.successful(false)
    }
}
