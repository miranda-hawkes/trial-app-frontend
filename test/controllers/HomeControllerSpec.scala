
package controllers

import auth.{AuthenticatedAction, AuthorisedActions}
import config.WSHttp
import models.Organisation
import org.mockito.ArgumentMatchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import play.api.http.{HeaderNames, Status}
import play.api.mvc.{Action, AnyContent, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.PartialsService
import traits.ControllerTestSpec
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.domain.{Accounts, Authority, ConfidenceLevel, CredentialStrength}
import uk.gov.hmrc.play.http.{HttpResponse, SessionKeys}

import scala.concurrent.Future

class HomeControllerSpec extends ControllerTestSpec with MockitoSugar {

  val mockHttp: WSHttp = mock[WSHttp]
  val mockService: PartialsService = mock[PartialsService]

  def setupController(authContext: AuthContext,
                      isAuthorised: Boolean): HomeController = {

    val mockAuthorisedActions: AuthorisedActions = mock[AuthorisedActions]

    if(isAuthorised)
      when(mockAuthorisedActions.authorisedOrganisationAction(ArgumentMatchers.any()))
      .thenAnswer(new Answer[Action[AnyContent]] {
        override def answer(invocation: InvocationOnMock): Action[AnyContent] = {
          val action = invocation.getArgument[AuthenticatedAction](0)
          val organisation = Organisation(authContext)
          Action.async(action(organisation))
        }
      })
    else
      when(mockAuthorisedActions.authorisedOrganisationAction(ArgumentMatchers.any()))
        .thenReturn(Action.async(Results.Redirect("unauthorised-route")))

    new HomeController(mockAuthorisedActions, mockHttp, mockService, messagesApi, mockConfig) {
      override lazy val nationalInsuranceUrl: String = ""
      override lazy val paymentsUrl: String = ""
    }
  }

  val authContext = AuthContext(
    Authority(
      "userId",
      Accounts(),
      None,
      None,
      CredentialStrength.Strong,
      ConfidenceLevel.L200,
      None,
      None,
      None,
      ""
    )
  )

  lazy val fakeRequest = FakeRequest("GET", "/")

  "HomeController .home" when {

    "user is authorised and has a valid referer in session" should {
      val controller = setupController(authContext, isAuthorised = true)

      when(mockService.getPartials(ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Seq(Future.successful(Html(""))))

      lazy val result = controller.home(fakeRequest.withHeaders((HeaderNames.REFERER, "")))

      "return 200" in {
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "user is authorised and has no referer in session" should {
      val controller = setupController(authContext, isAuthorised = true)

      when(mockService.getPartials(ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Seq(Future.successful(Html(""))))

      lazy val result = controller.home(fakeRequest)

      "return 200" in {
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "user is unauthorised" should {
      val controller = setupController(authContext, isAuthorised = false)
      lazy val result = controller.home(fakeRequest)

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to 'unauthorised-route'" in {
        redirectLocation(result) shouldBe Some("unauthorised-route")
      }
    }
  }
}
