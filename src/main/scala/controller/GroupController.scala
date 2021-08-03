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
              val saved = GroupService.create(group)
              processResult(saved)
            }
          }
        },
        delete {
          path("delete") {
            parameters("id") { id =>
              val deleted = GroupService.delete(id.toInt)
              processResult(deleted)
            }
          }
        },
        post {
          path("update") {
            entity(as[GroupDto]) { group =>
              val updated = GroupService.update(group)
              processResult(updated)
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

  def processResult(result: Future[Either[String, Int]]): Route = {
    onSuccess(result) {
      case Right(_) => complete("Success")
      case Left(x) => complete(x)
    }
  }
}
