package controllers

import javax.inject.{Inject, Singleton}

import config.{AppConfig, WSHttp}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._

import scala.concurrent.Future
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.http.HttpGet
import uk.gov.hmrc.play.partials.{CachedStaticHtmlPartialRetriever, HtmlPartial}

@Singleton
class HelloWorldController @Inject()(appConfig: AppConfig,
                                     val messagesApi: MessagesApi) extends FrontendController with I18nSupport with CachedStaticHtmlPartialRetriever {

  override def httpGet: HttpGet = WSHttp

  val helloWorld: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(views.html.helloWorld(appConfig, retrieveNIPartial.successfulContentOrEmpty)))
  }

  def retrieveNIPartial(implicit request: Request[_]): HtmlPartial = loadPartial("http://localhost:9000/trial-app-frontend/national-insurance")

}
