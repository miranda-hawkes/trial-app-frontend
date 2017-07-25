
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

class HelloWorldControllerSpec extends ControllerTestSpec with MockitoSugar {


  def setupController(authContext: AuthContext,
                      isAuthorised: Boolean): HelloWorldController = {

    val mockHttp = mock[WSHttp]
    val mockAuthorisedActions: AuthorisedActions = mock[AuthorisedActions]

    if(isAuthorised)
      when(mockAuthorisedActions.authorisedOrganisationAction(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenAnswer(new Answer[Action[AnyContent]] {
        override def answer(invocation: InvocationOnMock): Action[AnyContent] = {
          val action = invocation.getArgument[AuthenticatedAction](1)
          val organisation = Organisation(authContext)
          Action.async(action(organisation))
        }
      })
    else
      when(mockAuthorisedActions.authorisedOrganisationAction(ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Action.async(Results.Redirect("unauthorised-route")))

    new HelloWorldController(mockConfig, mockAuthorisedActions, mockHttp, messagesApi) {
      override lazy val nationalInsuranceUrl: String = "http://www.gov.uk"
      override lazy val paymentsUrl: String = "http://www.gov.uk"
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

  "HelloWorldController .helloWorld" when {

    "user is authorised" should {
      val controller = setupController(authContext, isAuthorised = true)
      lazy val result = controller.helloWorld("url")(fakeRequest)

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
      lazy val result = controller.helloWorld("url")(fakeRequest)

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to 'unauthorised-route'" in {
        redirectLocation(result) shouldBe Some("unauthorised-route")
      }
    }
  }

  "HelloWorldController .retrieveNIPartial" should {
    val controller = setupController(authContext, isAuthorised = true)
    lazy val result = controller.retrieveNIPartial(fakeRequest)

    "return some html" in {
      contentType(result) shouldBe HTML
    }
  }

  "HelloWorldController .retrievePaymentsPartial" should {
    val controller = setupController(authContext, isAuthorised = true)
    lazy val result = controller.retrievePaymentsPartial(fakeRequest)

    "return some html" in {
      contentType(result) shouldBe HTML
    }
  }
}
