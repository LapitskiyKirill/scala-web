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

class UserController(userService: UserService = new UserService) {
  implicit val userFormat: RootJsonFormat[UserDto] = jsonFormat6(UserDto)
  implicit val groupFormat: RootJsonFormat[GroupDto] = jsonFormat2(GroupDto)
  implicit val userGroupFormat: RootJsonFormat[UserGroup] = jsonFormat3(UserGroup)
  implicit val resultUserFormat: RootJsonFormat[ResultUser] = jsonFormat7(ResultUser)
  implicit val resultGroupFormat: RootJsonFormat[ResultGroup] = jsonFormat3(ResultGroup)
  implicit val responseFormat: RootJsonFormat[Response] = jsonFormat1(Response)

  def getRoute: Route = route

  val route: Route = concat(
    pathPrefix("user") {
      concat(
        post {
          entity(as[UserDto]) { user =>
            val saved = userService.create(user)
            processResult(201, saved)
          }
        },
        delete {
          path(Segment) { id =>
            val deleted = userService.deleteById(id.toInt)
            processResult(200, deleted)
          }
        },
        put {
          path(Segment) { id =>
            entity(as[UserDto]) { user =>
              val updated = userService.update(id.toInt, user)
              processResult(200, updated)
            }
          }
        },
        get {
          path(Segment) { id =>
            val result = userService.findById(id.toInt)
            onSuccess(result) { result =>
              complete(200, result)
            }
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
