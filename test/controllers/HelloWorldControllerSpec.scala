
package controllers

import org.scalatest.mockito.MockitoSugar
import play.api.http.Status
import play.api.i18n.MessagesApi
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import play.api.inject.Injector
import traits.FakeRequestHelper

class HelloWorldControllerSpec extends UnitSpec with MockitoSugar with WithFakeApplication with FakeRequestHelper {

  val injector: Injector = fakeApplication.injector
  val messagesApi: MessagesApi = injector.instanceOf[MessagesApi]
  val controller: HelloWorldController = new HelloWorldController(messagesApi)

  "HelloWorldController .helloWorld" should {
    lazy val result = controller.helloWorld(fakeRequest)

    "return 200" in {
      status(result) shouldBe Status.OK
    }

    "return HTML" in {
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }
}
