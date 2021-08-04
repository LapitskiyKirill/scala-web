package controller

import _root_.entity.Implicits._
import _root_.entity._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, onSuccess, post, _}
import akka.http.scaladsl.server.Route
import service.UserGroupService

import scala.concurrent.Future

class UserGroupController(userGroupService: UserGroupService = new UserGroupService()) {

  def getRoute: Route = route

  val route: Route = concat(
    pathPrefix("user-group") {
      concat(
        post {
          entity(as[UserGroup]) { userGroup =>
            val saved = userGroupService.addUserToGroup(userGroup)
            processResult(StatusCodes.Created, saved)
          }
        },
        delete {
          entity(as[UserGroup]) {
            userGroup =>
              val deleted = userGroupService.deleteUserFromGroup(userGroup)
              processResult(StatusCodes.NoContent, deleted)
          }
        }
      )
    }
  )

  def processResult(successStatusCode: StatusCode, result: Future[Either[String, String]]): Route = {
    onSuccess(result) {
      case Right(x) => complete(successStatusCode, Response(x))
      case Left(x) => complete(StatusCodes.InternalServerError, Response(x))
    }
  }
}
