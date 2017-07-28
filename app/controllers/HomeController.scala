package controllers

import javax.inject.{Inject, Singleton}

import auth.AuthorisedActions
import config.WSHttp
import config.AppConfig
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._

import scala.concurrent.Future
import play.api.i18n.{I18nSupport, MessagesApi}

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class HomeController @Inject()(authorisedActions: AuthorisedActions,
                               val wsHttp: WSHttp,
                               val messagesApi: MessagesApi,
                               implicit val appConfig: AppConfig) extends FrontendController with I18nSupport with PartialController {

  lazy val nationalInsuranceUrl: String = appConfig.nationalInsuranceUrl + "?errorCode=200"
  lazy val paymentsUrl: String = s"${appConfig.paymentsUrl}?submitUrl=$localSubmitUrl"

  override lazy val urls: Seq[String] = List(nationalInsuranceUrl, paymentsUrl)

  val home: Action[AnyContent] = authorisedActions.authorisedOrganisationAction {
    implicit request =>
      implicit user =>
        Future.sequence(getPartials).map {
          partial =>
            (user.session.get(REFERER), user.headers.get(REFERER)) match {
              case (None, Some(ref)) => Ok(views.html.home(partial)).withSession(user.session + (REFERER -> ref))
              case _ => Ok(views.html.home(partial))
          }
        }
  }

  val submitPayments: Action[AnyContent] = authorisedActions.authorisedOrganisationAction {
    implicit request =>
      implicit user =>
        Future.successful(Ok)
  }

  /* tell the partial where a submit action should go to */
  val localSubmitUrl = "http://localhost:9000/trial-app-frontend/home"

}
