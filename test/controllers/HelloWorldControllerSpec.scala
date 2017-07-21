
package controllers

import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import traits.ControllerTestSpec

class HelloWorldControllerSpec extends ControllerTestSpec {

  val controller: HelloWorldController = new HelloWorldController(mockConfig, messagesApi)
  lazy val fakeRequest = FakeRequest("GET", "/")

  "HelloWorldController .helloWorld" when {

    "calling .helloWorld" should {
      lazy val result = controller.helloWorld(fakeRequest)

      "return 200" in {
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "calling retrieveNIPartial" should {

      lazy val result = controller.retrieveNIPartial(fakeRequest)

      "return some html" in {
        contentType(result) shouldBe HTML
      }
    }

    "calling retrievePaymentsPartial" should {

      lazy val result = controller.retrievePaymentsPartial(fakeRequest)

      "return some html" in {
        contentType(result) shouldBe HTML
      }
    }
  }
}
