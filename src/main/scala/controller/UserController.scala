package controller

import _root_.entity.Implicits._
import _root_.entity._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, onSuccess, path, post, put, _}
import akka.http.scaladsl.server.Route
import service.{UserGroupService, UserService}

import scala.concurrent.Future

class UserController(
                      userService: UserService = new UserService,
                      userGroupService: UserGroupService = new UserGroupService
                    ) {

  def getRoute: Route = route

  val route: Route = concat(
    pathPrefix("user") {
      concat(
        post {
          entity(as[UserDto]) { user =>
            val saved = userService.create(user)
            processResult(StatusCodes.Created, saved)
          }
        },
        delete {
          path(Segment) { id =>
            val deleted = userService.deleteById(id.toInt)
            processResult(StatusCodes.NoContent, deleted)
          }
        },
        put {
          path(Segment) { id =>
            entity(as[UserDto]) { user =>
              val updated = userService.update(id.toInt, user)
              processResult(StatusCodes.OK, updated)
            }
          }
        },
        get {
          path(Segment) { id =>
            val result = userService.findById(id.toInt)
            onSuccess(result) { result =>
              complete(StatusCodes.OK, result)
            }
          }
        },
        put {
          path(IntNumber / "group" / IntNumber) { (userId, groupId) =>
            val saved = userGroupService.addUserToGroup(userId, groupId)
            processResult(StatusCodes.Created, saved)
          }
        },
        delete {
          path(IntNumber / "group" / IntNumber) { (userId, groupId) =>
            val saved = userGroupService.deleteUserFromGroup(userId, groupId)
            processResult(StatusCodes.Created, saved)
          }
        },
        get {
            val result = userService.getAll
            onSuccess(result) {
              f=>complete("ss")
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
