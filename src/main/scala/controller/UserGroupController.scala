package controller

import _root_.entity._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, onSuccess, path, post, _}
import akka.http.scaladsl.server.Route
import repository.BaseRepository
import service.UserGroupService
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.concurrent.Future

class UserGroupController {

  val baseRepository = new BaseRepository
  implicit val userGroupFormat: RootJsonFormat[UserGroup] = jsonFormat3(UserGroup)

  def getRoute: Route = route

  val route: Route = concat(
    pathPrefix("user-group") {
      concat(
        post {
          path("add") {
            entity(as[UserGroup]) { userGroup =>
              val saved = UserGroupService.addUserToGroup(userGroup)
              processResult(saved)
            }
          }
        },
        post {
          path("remove") {
            entity(as[UserGroup]) {
              userGroup =>
                val deleted = UserGroupService.deleteUserFromGroup(userGroup)
                processResult(deleted)
            }
          }
        }
      )
    }
  )

  def processResult(result: Future[Either[String, Int]]): Route = {
    onSuccess(result) {
      case Right(_) => complete("Success")
      case Left(x) => complete(x)
    }
  }
}
