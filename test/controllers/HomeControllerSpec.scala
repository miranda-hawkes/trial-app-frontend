
package controllers

import auth.{AuthenticatedAction, AuthorisedActions}
import config.WSHttp
import models.Organisation
import org.mockito.ArgumentMatchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import play.api.http.Status
import play.api.mvc.{Action, AnyContent, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import traits.ControllerTestSpec
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.domain.{Accounts, Authority, ConfidenceLevel, CredentialStrength}
import uk.gov.hmrc.play.http.HttpResponse

import scala.concurrent.Future

class HomeControllerSpec extends ControllerTestSpec with MockitoSugar {

  val mockHttp: WSHttp = mock[WSHttp]

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

    new HomeController(mockAuthorisedActions, mockHttp, messagesApi, mockConfig) {
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

    "user is authorised and both partial retrievals are successful" should {
      val controller = setupController(authContext, isAuthorised = true)

      when(mockHttp.GET[HttpResponse](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK, responseString = Some("some body"))))
        .thenReturn(Future.successful(HttpResponse(OK, responseString = Some("more body"))))

      lazy val result = controller.home(fakeRequest)

      "return 200" in {
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "user is authorised and one partial retrieval is unsuccessful" should {
      val controller = setupController(authContext, isAuthorised = true)

      when(mockHttp.GET[HttpResponse](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK, responseString = Some("some body"))))
        .thenReturn(Future.successful(HttpResponse(UNAUTHORIZED, responseString = Some("you are unauthorised"))))

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
