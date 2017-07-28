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
  lazy val paymentsUrl: String = appConfig.paymentsUrl

  override lazy val urls: Seq[String] = List(nationalInsuranceUrl, paymentsUrl)

  val home: Action[AnyContent] = authorisedActions.authorisedOrganisationAction {
    implicit request =>
      implicit user =>
        Future.sequence(getPartials).map(partial => Ok(views.html.home(partial)))
  }
}
