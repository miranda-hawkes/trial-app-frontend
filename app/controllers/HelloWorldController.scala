package controllers

import javax.inject.{Inject, Singleton}

import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import scala.concurrent.Future
import play.api.i18n.{I18nSupport, MessagesApi}

@Singleton
class HelloWorldController @Inject()(val messagesApi: MessagesApi) extends FrontendController with I18nSupport {

  val helloWorld: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(views.html.hello_world()))
  }
}
