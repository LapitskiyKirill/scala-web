package controller

import _root_.entity.UserImplicits.localDateFormat
import _root_.entity._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, onSuccess, path, post, _}
import akka.http.scaladsl.server.Route
import service.GroupService
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.concurrent.Future

class GroupController {
  implicit val userFormat: RootJsonFormat[UserDto] = jsonFormat6(UserDto)
  implicit val groupFormat: RootJsonFormat[GroupDto] = jsonFormat2(GroupDto)
  implicit val userGroupFormat: RootJsonFormat[UserGroup] = jsonFormat3(UserGroup)
  implicit val resultUserFormat: RootJsonFormat[ResultUser] = jsonFormat7(ResultUser)
  implicit val resultGroupFormat: RootJsonFormat[ResultGroup] = jsonFormat3(ResultGroup)

  def getRoute: Route = route

  val route: Route = concat(
    pathPrefix("group") {
      concat(
        post {
          path("create") {
            entity(as[GroupDto]) { group =>
              val saved: Future[Int] = GroupService.create(group)
              onSuccess(saved) { _ =>
                complete("group created")
              }
            }
          }
        },
        delete {
          path("delete") {
            parameters("id") { id =>
              val deleted = GroupService.delete(id.toInt)
              onSuccess(deleted) { _ =>
                complete("group deleted")
              }
            }
          }
        },
        post {
          path("update") {
            entity(as[GroupDto]) { group =>
              val updated = GroupService.update(group)
              onSuccess(updated) { _ =>
                complete("group updated")
              }
            }
          }
        },
        get {
          path("find") {
            parameters("id") { id =>
              val result = GroupService.findById(id.toInt)
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
