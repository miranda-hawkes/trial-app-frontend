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

package auth

import javax.inject.Inject

import config.{ApplicationConfig, FrontendAuthConnector}
import connectors.FrontendAuthorisationConnector
import models.Organisation
import play.api.mvc.{Action, AnyContent}
import predicates.VisibilityPredicate
import uk.gov.hmrc.play.frontend.auth.{Actions, AuthContext, AuthenticationProvider, TaxRegime}
import services.AuthorisationService
import uk.gov.hmrc.play.frontend.auth.connectors.domain.Accounts

class AuthorisedActions @Inject()(appConfig: ApplicationConfig,
                                  authorisationService: AuthorisationService,
                                  frontendAuthorisationConnector: FrontendAuthorisationConnector) extends Actions {

  override val authConnector: FrontendAuthorisationConnector = frontendAuthorisationConnector

  private def composeAction(redirect: Option[String]) = {
    val redirectUrl: String = redirect match { case Some(url) => s"?redirect=$url" }
    val postSignInRedirectUrl: String = controllers.routes.HelloWorldController.helloWorld(redirectUrl).url
    val ggProvider = new GovernmentGatewayProvider(postSignInRedirectUrl, appConfig.governmentGateway)

    val regime = new Regime {
      override def authenticationType: AuthenticationProvider = ggProvider
    }

    lazy val visibilityPredicate = new VisibilityPredicate(authorisationService)
    lazy val guardedAction: AuthenticatedBy = AuthorisedFor(regime, visibilityPredicate)

    val authenticationAction: AuthenticatedAction => Action[AnyContent] = action => {
      guardedAction.async {
        authContext: AuthContext =>
          implicit request =>
            action(Organisation(authContext))(request)
      }
    }

    authenticationAction
  }

  def authorisedOrganisationAction(redirect: Option[String] = None)(action: AuthenticatedAction): Action[AnyContent] =
    composeAction(redirect)(action)

  trait Regime extends TaxRegime {
    override def isAuthorised(accounts: Accounts): Boolean = true

    override def authenticationType: AuthenticationProvider

    override def unauthorisedLandingPage: Option[String] = Some("http://www.google.co.uk")
  }
}
