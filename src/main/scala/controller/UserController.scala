package controller

import _root_.entity.UserImplicits.localDateFormat
import _root_.entity._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, onSuccess, path, post, _}
import akka.http.scaladsl.server.Route
import service.UserService
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.concurrent.Future

class UserController {
  implicit val userFormat: RootJsonFormat[UserDto] = jsonFormat6(UserDto)
  implicit val groupFormat: RootJsonFormat[GroupDto] = jsonFormat2(GroupDto)
  implicit val userGroupFormat: RootJsonFormat[UserGroup] = jsonFormat3(UserGroup)
  implicit val resultUserFormat: RootJsonFormat[ResultUser] = jsonFormat7(ResultUser)
  implicit val resultGroupFormat: RootJsonFormat[ResultGroup] = jsonFormat3(ResultGroup)

  def getRoute: Route = route

  val route: Route = concat(
    pathPrefix("user") {
      concat(
        post {
          path("create") {
            entity(as[UserDto]) { user =>
              val saved: Future[Int] = UserService.create(user)
              onSuccess(saved) { _ =>
                complete("user created")
              }
            }
          }
        },
        delete {
          path("delete") {
            parameters("id") { id =>
              val deleted = UserService.deleteById(id.toInt)
              onSuccess(deleted) { _ =>
                complete("user deleted")
              }
            }
          }
        },
        post {
          path("update") {
            entity(as[UserDto]) { user =>
              val updated = UserService.update(user)
              onSuccess(updated) { _ =>
                complete("user updated")
              }
            }
          }
        },
        get {
          path("find") {
            parameters("id") { id =>
              val result = UserService.findById(id.toInt)
              onSuccess(result) { result =>
                complete(result)
              }
            }
          }
        }
      )
    }
  )
}
