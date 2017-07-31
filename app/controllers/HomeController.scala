package controllers

import javax.inject.{Inject, Singleton}

import auth.AuthorisedActions
import config.WSHttp
import config.AppConfig
import models.ServiceModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import scala.concurrent.Future
import play.api.i18n.{I18nSupport, MessagesApi}
import play.twirl.api.Html
import services.PartialsService

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class HomeController @Inject()(authorisedActions: AuthorisedActions,
                               val wsHttp: WSHttp,
                               partialsService: PartialsService,
                               val messagesApi: MessagesApi,
                               implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  lazy val nationalInsuranceUrl: String = appConfig.nationalInsuranceUrl + "?errorCode=200"
  lazy val paymentsUrl: String = s"${appConfig.paymentsUrl}?submitUrl=$localSubmitUrl"
  lazy val paymentsService: ServiceModel = ServiceModel("Payments", paymentsUrl)
  lazy val nationalInsuranceService: ServiceModel = ServiceModel("National Insurance", nationalInsuranceUrl)
  lazy val services: Seq[ServiceModel] = Seq(nationalInsuranceService, paymentsService)

  /* tell the partial where a submit action should go to */
  val localSubmitUrl = "http://localhost:9000/trial-app-frontend/home"

  val home: Action[AnyContent] = authorisedActions.authorisedOrganisationAction {
    implicit request =>
      implicit user =>
        Future.sequence(partialsService.getPartials(services)).map {
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
        partialsService.postForm(paymentsService).map { response =>
          response.status match {
            case OK => Ok(views.html.home(Seq(Html(response.body))))
            case BAD_REQUEST => BadRequest
            case _ => InternalServerError
          }
        }
  }
}
