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

import config.WSHttp
import play.api.i18n.I18nSupport
import play.twirl.api.Html
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.partials.HtmlPartial
import uk.gov.hmrc.play.partials.HtmlPartial.{Failure, HtmlPartialHttpReads, Success}
import partials.html.errorText
import scala.concurrent.Future

trait PartialController extends FrontendController with I18nSupport with HtmlPartialHttpReads {

  val urls: Seq[String]
  val wsHttp: WSHttp

  def getPartials(implicit hc: HeaderCarrier): Seq[Future[Html]] = {
    urls.map(url =>
      for {
        response <- getPartial(url)
        partial <- handlePartial(response)
      } yield partial
    )
  }

  implicit val readPartialsForm: HttpReads[HttpResponse] = new HttpReads[HttpResponse] {
    def read(method: String, url: String, response: HttpResponse): HttpResponse = response
  }

  def getPartial(url: String)(implicit hc: HeaderCarrier): Future[HtmlPartial] = {
    wsHttp.GET[HttpResponse](url)(readPartialsForm, hc).map {
      response => read("GET", url, response)
    }
  }

  def handlePartial(htmlPartial: HtmlPartial): Future[Html] = {
    htmlPartial match {
      case Success(_, content) => Future.successful(content)
      case Failure(Some(status), _) => Future.successful(handleFailure(status))
    }
  }

  def handleFailure(status: Int): Html = {
    status match {
      case UNAUTHORIZED => errorText(s"$status You are not authorised to view this page")
      case INTERNAL_SERVER_ERROR => errorText(s"$status Please try again later.")
      case _ => errorText(s"$status An unknown error has occured")
    }
  }
}
