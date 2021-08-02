package controller

import _root_.entity._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, onSuccess, path, post, _}
import akka.http.scaladsl.server.Route
import repository.BaseRepository
import slick.jdbc.PostgresProfile.api._
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.concurrent.ExecutionContext.Implicits.global

class UserGroupController {

  val baseRepository = new BaseRepository
  implicit val userGroupFormat: RootJsonFormat[UserGroup] = jsonFormat3(UserGroup)

  def getRoute: Route = route

  val route: Route = concat(
    post {
      path("add-user-to-group") {
        entity(as[UserGroup]) { userGroup =>
          val user = baseRepository.exists[User, UserTable](Tables.users.filter(_.id === userGroup.userId))
          val group = baseRepository.exists[Group, GroupTable](Tables.groups.filter(_.id === userGroup.groupId))
          val saved = user.flatMap(u => {
            group.map(g => {
              if (g && u)
                Right(baseRepository.save[UserGroup, UserGroupTable](userGroup, Tables.userGroup))
              else
                Left("fail")
            })
          })
          onSuccess(saved) {
            case Right(_) => complete("Success")
            case Left(x) => complete(x)
          }
        }
      }
    },
    post {
      path("delete-user-from-group") {
        entity(as[UserGroup]) { userGroup =>
          val user = baseRepository.exists[User, UserTable](Tables.users.filter(_.id === userGroup.userId))
          val group = baseRepository.exists[Group, GroupTable](Tables.groups.filter(_.id === userGroup.groupId))
          val saved = user.flatMap(u => {
            group.map(g => {
              if (g && u)
                Right(baseRepository.delete[UserGroup, UserGroupTable](Tables.userGroup.filter(_.id === userGroup.id)))
              else
                Left("fail")
            })
          })
          onSuccess(saved) {
            case Right(_) => complete("Success")
            case Left(x) => complete(x)
          }
        }
      }
    }
  )
}
