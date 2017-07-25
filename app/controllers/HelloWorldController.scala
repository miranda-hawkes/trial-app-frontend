package controllers

import javax.inject.{Inject, Singleton}
import auth.AuthorisedActions
import config.WSHttp
import config.AppConfig
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import scala.concurrent.Future
import play.api.i18n.{I18nSupport, MessagesApi}
import play.twirl.api.Html

@Singleton
class HelloWorldController @Inject()(appConfig: AppConfig,
                                     authorisedActions: AuthorisedActions,
                                     val wsHttp: WSHttp,
                                     val messagesApi: MessagesApi) extends FrontendController with I18nSupport with PartialController {


  lazy val nationalInsuranceUrl: String = appConfig.nationalInsuranceUrl + "?errorCode=401"
  lazy val paymentsUrl: String = appConfig.paymentsUrl

  override lazy val urls: Seq[String] = List(nationalInsuranceUrl, paymentsUrl)

  val helloWorld: String => Action[AnyContent] = redirect => authorisedActions.authorisedOrganisationAction(Some(redirect)) {
    implicit request =>
      implicit user =>
        Future.sequence(getPartials).map(partial => Ok(views.html.helloWorld(appConfig, partial)))
  }
}
