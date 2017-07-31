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

import javax.inject.Inject

import config.WSHttp
import models.ServiceModel
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.mvc.{AnyContent, Request, RequestHeader}
import play.filters.csrf.CSRF
import uk.gov.hmrc.play.frontend.filters.SessionCookieCryptoFilter
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.partials.{HeaderCarrierForPartialsConverter, HtmlPartial}
import uk.gov.hmrc.play.partials.HtmlPartial.HtmlPartialHttpReads
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PartialsConnector @Inject()(http: WSHttp) extends HtmlPartialHttpReads with HeaderCarrierForPartialsConverter {

  override def crypto: (String) => String = SessionCookieCryptoFilter.encrypt

  def getPartial(service: ServiceModel)(implicit request: Request[AnyContent]): Future[HtmlPartial] = {
    http.GET[HttpResponse](getUrl(service.url)(request))(read, appendHeaderCarrier).map {
      response => read("GET", service.url, response)
    }
  }

  def postForm(service: ServiceModel)(implicit request: Request[AnyContent]): Future[HttpResponse] = {
    request.body.asFormUrlEncoded.map { formData =>
      http.POSTForm[HttpResponse](service.url, formData)(read, appendHeaderCarrier)
    }.getOrElse {
      Future.successful(HttpResponse(INTERNAL_SERVER_ERROR))
    }
  }

  private def getUrl(url: String)(implicit request: RequestHeader): String =
    s"$url&csrfToken=$getCsrfToken"

  private def getCsrfToken(implicit request: RequestHeader): String = {
    CSRF.getToken(request).map{ _.value }.getOrElse("")
  }

  private def appendHeaderCarrier(implicit request: Request[_]): HeaderCarrier ={
    val hc = headerCarrierEncryptingSessionCookieFromRequest(request)
    hc.toHeaderCarrier.withExtraHeaders("Csrf-Token" -> "nocheck") //needed for POST requests only
  }

  implicit val read: HttpReads[HttpResponse] = new HttpReads[HttpResponse] {
    def read(method: String, url: String, response: HttpResponse): HttpResponse = response
  }
}
