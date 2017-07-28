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
import play.twirl.api.Html
import play.api.http.Status._
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.partials.{HeaderCarrierForPartialsConverter, HtmlPartial}
import uk.gov.hmrc.play.partials.HtmlPartial.{Failure, HtmlPartialHttpReads, Success}
import partials.html.errorText
import play.api.mvc.{Request, RequestHeader}
import uk.gov.hmrc.play.frontend.filters.SessionCookieCryptoFilter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait PartialController extends HtmlPartialHttpReads with HeaderCarrierForPartialsConverter {

  val urls: Seq[String]
  val wsHttp: WSHttp
  override def crypto: (String) => String = SessionCookieCryptoFilter.encrypt

  private def partialsHeaderCarrier(implicit request: Request[_]): HeaderCarrier ={
    val hc = headerCarrierEncryptingSessionCookieFromRequest(request)
    hc.toHeaderCarrier.withExtraHeaders("Csrf-Token" -> "nocheck") //needed for POST requests only
  }

  implicit val read: HttpReads[HttpResponse] = new HttpReads[HttpResponse] {
    def read(method: String, url: String, response: HttpResponse): HttpResponse = response
  }

  def getPartials(implicit request: Request[_]): Seq[Future[Html]] = {
    urls.map(url =>
      for {
        response <- getPartial(url)
        partial <- handlePartial(response)
      } yield partial
    )
  }

  protected[controllers] def getPartial(url: String)(implicit request: Request[_]): Future[HtmlPartial] = {
    wsHttp.GET[HttpResponse](url)(read, partialsHeaderCarrier).map {
      response =>
        read("GET", url, response)
    }
  }

  protected[controllers] def handlePartial(htmlPartial: HtmlPartial): Future[Html] = {
    htmlPartial match {
      case Success(_, content) => Future.successful(content)
      case Failure(Some(status), _) => Future.successful(handleFailure(status))
    }
  }

  /* some examples */
  protected[controllers] def handleFailure(status: Int): Html = {
    status match {
      case UNAUTHORIZED => errorText(s"$status You are not authorised to view this page")
      case INTERNAL_SERVER_ERROR => errorText(s"$status Please try again later.")
      case _ => errorText(s"$status An unknown error has occurred")
    }
  }
}
