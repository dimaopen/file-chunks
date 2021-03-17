package dopenkov.filechunks

import akka.http.scaladsl.model.{ContentTypes, Multipart}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}

import java.io.File

class FullTestKitExampleSpec extends WordSpec with Matchers with ScalatestRouteTest {

  "The service" should {

    "return a hash for a short file" in {
      val file = new File(getClass.getResource("/test-file-1.txt").toURI)
      val formData = Multipart.FormData.fromFile("data_file", ContentTypes.`application/octet-stream`, file, 100000)
      Post("/api/hash", formData) ~> StreamingHashes.route(256) ~> check {
        responseAs[String] shouldEqual """["437c6a8eb8db12c70113ddfbd756fc582e167276937d2821463a45f6494ac599"]"""
      }
    }

    "return 2 hashes for file greater then chunksize" in {
      val file = new File(getClass.getResource("/test-file-1.txt").toURI)
      val formData = Multipart.FormData.fromFile("data_file", ContentTypes.`application/octet-stream`, file, 100000)
      Post("/api/hash", formData) ~> StreamingHashes.route(23) ~> check {
        responseAs[String] shouldEqual
          """["2da99a9d19436268bb93ee90c3f5d3038289f9db27366c9de6778e7d7d3b9d3a","c5406c1d801d0161be42405172cb28232057f71a4bf91db7bca944b9af48252a"]"""
      }
    }
  }
}