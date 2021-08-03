package controller

import _root_.entity._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, onSuccess, post, _}
import akka.http.scaladsl.server.Route
import service.UserGroupService
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.concurrent.Future

class UserGroupController(userGroupService: UserGroupService = new UserGroupService()) {

  implicit val userGroupFormat: RootJsonFormat[UserGroup] = jsonFormat3(UserGroup)
  implicit val responseFormat: RootJsonFormat[Response] = jsonFormat1(Response)

  def getRoute: Route = route

  val route: Route = concat(
    pathPrefix("user-group") {
      concat(
        post {
          entity(as[UserGroup]) { userGroup =>
            val saved = userGroupService.addUserToGroup(userGroup)
            processResult(200, saved)
          }
        },
        delete {
          entity(as[UserGroup]) {
            userGroup =>
              val deleted = userGroupService.deleteUserFromGroup(userGroup)
              processResult(200, deleted)
          }
        }
      )
    }
  )

  def processResult(successStatusCode: Int, result: Future[Either[String, String]]): Route = {
    onSuccess(result) {
      case Right(x) => complete(successStatusCode, Response(x))
      case Left(x) => complete(500, Response(x))
    }
  }
}
