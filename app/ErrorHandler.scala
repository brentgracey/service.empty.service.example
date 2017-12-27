import java.util.logging.Logger

import play.api.http.HttpErrorHandler
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent._
import javax.inject.Singleton;

@Singleton
class ErrorHandler extends HttpErrorHandler {

  val logger = Logger.getLogger(this.getClass.getName)

  def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
    Future.successful(
      Status(statusCode)("Error: " + message)
    )
  }

  def onServerError(request: RequestHeader, exception: Throwable) = {

    exception.printStackTrace()

    Future.successful(
      InternalServerError("A server error occurred: " + exception.getMessage)
    )
  }
}