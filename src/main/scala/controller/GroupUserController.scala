package controller

import _root_.entity.Implicits._
import _root_.entity._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, onSuccess, post, _}
import akka.http.scaladsl.server.Route
import service.GroupUsersService

import scala.concurrent.Future

class GroupUserController(groupUsersService: GroupUsersService = new GroupUsersService) {

  def getRoute: Route = route

  val route: Route = concat(
    pathPrefix("grouped") {
      post {
        entity(as[GroupUsers]) { groupUsers =>
          val saved = groupUsersService.save(groupUsers)
          processResult(StatusCodes.Created, saved)
        }
      }
    }
  )

  def processResult(successStatusCode: StatusCode, result: Future[Either[String, String]]): Route = {
    onSuccess(result) {
      case Right(x) => complete(successStatusCode, Response(x))
      case Left(x) => complete(StatusCodes.InternalServerError, Response(x))
    }
  }
}
