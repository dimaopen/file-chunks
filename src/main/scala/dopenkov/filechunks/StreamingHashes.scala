package dopenkov.filechunks

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream._
import akka.stream.scaladsl._
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.util.ByteString
import com.typesafe.scalalogging.StrictLogging
import org.apache.commons.codec.binary.Hex
import spray.json.DefaultJsonProtocol

import java.security.MessageDigest
import scala.concurrent.Future

object StreamingHashes extends SprayJsonSupport with DefaultJsonProtocol with StrictLogging {

  val API_ROOT = "api"

  def route(chunkSize: Int): Route = {

    path(API_ROOT / "hash") {
      extractRequestContext { ctx =>
        implicit val materializer: Materializer = ctx.materializer
        val digest = MessageDigest.getInstance("SHA-256")

        fileUpload("data_file") { case (metadata, byteSource) =>
          logger.info(s"Received file ${metadata.fileName}")
          val result: Future[IndexedSeq[String]] =
            byteSource.via(Flow[ByteString].via(new DelimiterFramingStage(chunkSize)))
              .map { bs =>
                digest.reset()
                digest.digest(bs.toArray)
              }
              .map(bytes => Hex.encodeHexString(bytes))
              .runFold(IndexedSeq.empty[String]) { (acc, hex) => acc :+ hex }

          onSuccess(result) { strings => complete(strings) }
        }
      }
    }
  }

  def testRoute: Route = {
    path("upload_test.html") {
      getFromResource("upload.html") // uses implicit ContentTypeResolver
    }
  }

}

private class DelimiterFramingStage(val messageLength: Int)
  extends GraphStage[FlowShape[ByteString, ByteString]] {
  private val in = Inlet[ByteString]("LengthFramingStage.in")
  private val out = Outlet[ByteString]("LengthFramingStage.out")
  override val shape: FlowShape[ByteString, ByteString] = FlowShape(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) with InHandler with OutHandler {
      setHandlers(in, out, this)
      private var buffer = ByteString.empty

      override def onPush(): Unit = {
        buffer ++= grab(in)
        if (buffer.length >= messageLength) {
          pushAppropriateData()
        } else {
          pull(in)
        }
      }

      override def onPull(): Unit = {
        if (buffer.length < messageLength && !isClosed(in)) pull(in)
        else pushAppropriateData()
      }

      override def onUpstreamFinish(): Unit = {
        if (buffer.isEmpty) {
          completeStage()
        } else if (isAvailable(out)) {
          pushAppropriateData()
        } // else swallow the termination and wait for pull
      }


      private def pushAppropriateData(): Unit = {
        val (msg, tail) = buffer.splitAt(messageLength)
        buffer = tail.compact
        push(out, msg)
        if(buffer.isEmpty && isClosed(in)) {
          completeStage()
        }
      }
    }

}