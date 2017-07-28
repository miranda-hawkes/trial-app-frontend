package config

import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc._
import play.api.{Application, Configuration, Play}
import play.mvc.Http.HeaderNames
import play.twirl.api.Html
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.play.audit.filters.FrontendAuditFilter
import uk.gov.hmrc.play.config.{AppName, ControllerConfig, RunMode}
import uk.gov.hmrc.play.filters.MicroserviceFilterSupport
import uk.gov.hmrc.play.frontend.bootstrap.DefaultFrontendGlobal
import uk.gov.hmrc.play.http.logging.filters.FrontendLoggingFilter

import scala.concurrent.Future

object FrontendGlobal extends DefaultFrontendGlobal with RunMode {

  override val auditConnector = FrontendAuditConnector
  override val loggingFilter = LoggingFilter
  override val frontendAuditFilter = AuditFilter
  override def filters: Seq[EssentialFilter] = frontendFilters :+ CorsFilter

  override def onStart(app: Application) {
    super.onStart(app)
    ApplicationCrypto.verifyConfiguration()
  }

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit rh: Request[_]): Html = {
    val appConfig: AppConfig = Play.current.injector.instanceOf[AppConfig]
    views.html.error_template(pageTitle, heading, message, appConfig)
  }

  override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = app.configuration.getConfig(s"microservice.metrics")
}

object ControllerConfiguration extends ControllerConfig {
  lazy val controllerConfigs: Config = Play.current.configuration.underlying.as[Config]("controllers")
}

object LoggingFilter extends FrontendLoggingFilter with MicroserviceFilterSupport {
  override def controllerNeedsLogging(controllerName: String): Boolean = ControllerConfiguration.paramsForController(controllerName).needsLogging
}

object AuditFilter extends FrontendAuditFilter with RunMode with AppName with MicroserviceFilterSupport {

  override lazy val maskedFormFields = Seq("password")
  override lazy val applicationPort = None
  override lazy val auditConnector = FrontendAuditConnector

  override def controllerNeedsAuditing(controllerName: String): Boolean = ControllerConfiguration.paramsForController(controllerName).needsAuditing
}

object CorsFilter extends Filter with MicroserviceFilterSupport {
  import scala.concurrent.ExecutionContext.Implicits.global

  def apply(nextFilter: (RequestHeader) => Future[Result])(requestHeader: RequestHeader): Future[Result] = {

    nextFilter(requestHeader).map { result =>
      val exposedHeaders: String = "Location, Origin, X-Requested-With, Content-Type, Accept, Referer, User-Agent"
      result.withHeaders(HeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN -> "*",
        HeaderNames.ALLOW -> "*",
        HeaderNames.ACCESS_CONTROL_ALLOW_METHODS -> "POST, GET, PUT, DELETE, OPTIONS",
        HeaderNames.ACCESS_CONTROL_ALLOW_HEADERS -> exposedHeaders,
        HeaderNames.ACCESS_CONTROL_EXPOSE_HEADERS -> exposedHeaders
      )
    }
  }
}
