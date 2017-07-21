package controllers

import javax.inject.{Inject, Singleton}

import config.{AppConfig, WSHttp}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._

import scala.concurrent.Future
import play.api.i18n.{I18nSupport, MessagesApi}
import play.twirl.api.Html
import uk.gov.hmrc.play.http.HttpGet
import uk.gov.hmrc.play.partials.CachedStaticHtmlPartialRetriever

@Singleton
class HelloWorldController @Inject()(appConfig: AppConfig,
                                     val messagesApi: MessagesApi) extends FrontendController with I18nSupport with CachedStaticHtmlPartialRetriever {

  override def httpGet: HttpGet = WSHttp

  val helloWorld: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(views.html.helloWorld(appConfig, Seq(retrieveNIPartial, retrievePaymentsPartial))))
  }

  def retrieveNIPartial(implicit request: Request[_]): Html = {
    loadPartial(appConfig.nationalInsuranceUrl).successfulContentOrElse(Html("<h1>Unable to retrieve NI data</h1>"))
  }

  def retrievePaymentsPartial(implicit request: Request[_]): Html = {
    loadPartial(appConfig.paymentsUrl).successfulContentOrElse(Html("<h1>Unable to retrieve payments data</h1>"))
  }

}
