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

import play.api.mvc.{AnyContent, Request}
import uk.gov.hmrc.play.frontend.auth._
import play.api.mvc.Results._
import services.AuthorisationService
import uk.gov.hmrc.play.http.HeaderCarrier
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AffinityGroupPredicate(authorisationService: AuthorisationService) extends PageVisibilityPredicate {

  //TODO: change
  private val errorAction = Future.successful(Redirect("http://www.gov.uk"))

  private val isOrganisation = (affinityGroup: String) => Future.successful(affinityGroup == "Organisation")

  private def checkAffinityGroup(affinityGroup: String): Future[PageVisibilityResult] = {
    isOrganisation(affinityGroup).map { check =>
      if(check) PageIsVisible
      else PageBlocked(errorAction)
    }
  }

  private def routeRequest(affinityGroup: Option[String]): Future[PageVisibilityResult] = affinityGroup match {
    case Some(data) => checkAffinityGroup(data)
    case _ => Future.successful(PageBlocked(errorAction))
  }

  override def apply(authContext: AuthContext, request: Request[AnyContent]): Future[PageVisibilityResult] = {

    implicit val hc = (request: Request[_]) => HeaderCarrier.fromHeadersAndSession(request.headers, Some(request.session))

    val affinityGroup: Future[Option[String]] = authorisationService.getAffinityGroup(hc(request))

    for {
      affinityGroup <- affinityGroup
      visibility <- routeRequest(affinityGroup)
    } yield visibility
  }
}
