package config

import javax.inject.{Inject, Singleton}

import uk.gov.hmrc.play.audit.http.config.LoadAuditingConfig
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector => Auditing}
import uk.gov.hmrc.play.config.{AppName, RunMode, ServicesConfig}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.http.ws._
import uk.gov.hmrc.http.cache.client.SessionCache

@Singleton
class FrontendAuditConnector @Inject()() extends Auditing with AppName {
  override lazy val auditingConfig = LoadAuditingConfig(s"auditing")
}

@Singleton
class FrontendAuthConnector @Inject()(override val http: WSHttp) extends AuthConnector with ServicesConfig {
  lazy val serviceUrl: String = baseUrl("auth")
}

object FrontendAuditConnector extends Auditing with AppName {
  override lazy val auditingConfig = LoadAuditingConfig(s"auditing")
}

object FrontendAuthConnector extends AuthConnector with ServicesConfig {

  lazy val http = WSHttp
  override val serviceUrl: String = baseUrl("auth")

  object WSHttp extends WSGet with WSPut with WSPost with WSDelete with AppName with RunMode {
    override val hooks = NoneRequired
  }
}

@Singleton
class AppSessionCache @Inject()(override val http: WSHttp, appConfig: AppConfig) extends SessionCache with ServicesConfig with AppName {
  override lazy val domain: String = getConfString("cachable.session-cache.domain", throw new Exception(""))
  override lazy val baseUri: String = baseUrl("cachable.session-cache")
  override lazy val defaultSource: String = appName
}
