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

package services

import com.google.inject.Inject
import connectors.PartialsConnector
import models.ServiceModel
import partials.html._
import play.api.http.Status.{INTERNAL_SERVER_ERROR, UNAUTHORIZED}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{AnyContent, Request}
import play.twirl.api.Html
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.play.partials.HtmlPartial
import uk.gov.hmrc.play.partials.HtmlPartial.{Failure, Success}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PartialsService @Inject()(partialsConnector: PartialsConnector,
                                val messagesApi: MessagesApi) extends I18nSupport {

  def getPartials(services: Seq[ServiceModel])(implicit request: Request[AnyContent]): Seq[Future[Html]] = {
    services.map(service =>
      for {
        response <- partialsConnector.getPartial(service)
        partial <- handlePartial(response, service)
      } yield partial
    )
  }

  def postForm(service: ServiceModel)(implicit request: Request[AnyContent]): Future[HttpResponse] = {
    partialsConnector.postForm(service)
  }

  protected[services] def handlePartial(htmlPartial: HtmlPartial, service: ServiceModel): Future[Html] = {
    htmlPartial match {
      case Success(_, content) => Future.successful(content)
      case Failure(Some(status), _) => Future.successful(handleFailure(status, service))
    }
  }

  /* some examples of potential error handling */
  protected[services] def handleFailure(status: Int, service: ServiceModel): Html = {
    status match {
      case UNAUTHORIZED => errorConfidenceLevel(service.serviceName)
      case INTERNAL_SERVER_ERROR => errorText(s"$status Please try again later.")
      case _ => errorText(s"$status An unknown error has occurred")
    }
  }
}
