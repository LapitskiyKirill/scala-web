package controller

import _root_.entity.Implicits._
import _root_.entity._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, onSuccess, path, post, _}
import akka.http.scaladsl.server.Route
import service.GroupService

import scala.concurrent.Future

class GroupController(groupService: GroupService = new GroupService) {

  def getRoute: Route = route

  val route: Route = concat(
    pathPrefix("group") {
      concat(
        post {
          entity(as[GroupDto]) { group =>
            val saved = groupService.create(group)
            processResult(StatusCodes.Created, saved)
          }
        },
        delete {
          path(Segment) { id =>
            val deleted = groupService.delete(id.toInt)
            processResult(StatusCodes.NoContent, deleted)
          }
        },
        put {
          path(Segment) { id =>
            entity(as[GroupDto]) { group =>
              val updated = groupService.update(id.toInt, group)
              processResult(StatusCodes.OK, updated)
            }
          }
        },
        get {
          path(Segment) { id =>
            val result = groupService.findById(id.toInt)
            onSuccess(result) { result =>
              complete(StatusCodes.OK, result)
            }
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
