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

package controllers

import javax.inject.Inject

import auth.AuthorisedActions
import forms.TransactionForm
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Result}
import predicates.ConfidenceLevelCheck.confidenceLevelCheck
import services.AuthorisationService
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

class PaymentsController @Inject()(authorisedActions: AuthorisedActions,
                                   authorisationService: AuthorisationService,
                                   val messagesApi: MessagesApi) extends FrontendController with I18nSupport {

  def payments(submitUrl: String, csrfToken: String): Action[AnyContent] = Action.async { implicit request =>

    val response: Boolean => Future[Result] = isAuthorised => {
      if(isAuthorised) Future.successful(Ok(partials.html.payments(csrfToken, submitUrl)))
      else Future.successful(Unauthorized)
    }

    for {
      authContext <- authorisationService.getAuthority()
      isAuthorised <- confidenceLevelCheck(authContext)
      route <- response(isAuthorised)
    } yield route
  }

  def submit(submitUrl: String): Action[AnyContent] = Action.async {
    implicit request =>

      val response: Boolean => Future[Result] = isAuthorised => {
        if(isAuthorised) {
          TransactionForm.transactionForm.bindFromRequest().fold(
            error => Future.successful(BadRequest),
            data => {
              Future.successful(Ok(partials.html.paymentDetails(submitUrl, data.id)))
            }
          )
        }
        else Future.successful(Unauthorized)
      }

      for {
        authContext <- authorisationService.getAuthority()
        isAuthorised <- confidenceLevelCheck(authContext)
        route <- response(isAuthorised)
      } yield route
  }
}
