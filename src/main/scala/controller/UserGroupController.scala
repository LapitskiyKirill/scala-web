package controller

import _root_.entity._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, onSuccess, path, post, _}
import akka.http.scaladsl.server.Route
import repository.BaseRepository
import service.UserGroupService
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

class UserGroupController {

  val baseRepository = new BaseRepository
  implicit val userGroupFormat: RootJsonFormat[UserGroup] = jsonFormat3(UserGroup)

  def getRoute: Route = route

  val route: Route = concat(
    post {
      path("add-user-to-group") {
        entity(as[UserGroup]) { userGroup =>
          val saved = UserGroupService.addUserToGroup(userGroup)
          onSuccess(saved) {
            case Right(_) => complete("Success")
            case Left(x) => complete(x)
          }
        }
      }
    },
    post {
      path("delete-user-from-group") {
        entity(as[UserGroup]) {
          userGroup =>
            val deleted = UserGroupService.deleteUserFromGroup(userGroup)
            onSuccess(deleted) {
              case Right(_) => complete("Success")
              case Left(x) => complete(x)
            }
        }
      }
    }
  )
}
