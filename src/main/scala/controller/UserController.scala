package controller

import _root_.entity._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, onSuccess, path, post, _}
import akka.http.scaladsl.server.Route
import repository.{BaseRepository, GroupRepository, UserGroupRepository, UserRepository}
import slick.jdbc.PostgresProfile.api._
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserController {

  val userRepository = new UserRepository
  val groupRepository = new GroupRepository
  val baseRepository = new BaseRepository
  val userGroupRepository = new UserGroupRepository
  implicit val userFormat: RootJsonFormat[User] = jsonFormat6(User)
  implicit val groupFormat: RootJsonFormat[Group] = jsonFormat2(Group)
  implicit val userGroupFormat: RootJsonFormat[UserGroup] = jsonFormat3(UserGroup)
  implicit val resultUserFormat: RootJsonFormat[ResultUser] = jsonFormat7(ResultUser)
  implicit val resultGroupFormat: RootJsonFormat[ResultGroup] = jsonFormat3(ResultGroup)

  def getRoute: Route = route

  val route: Route = concat(
    post {
      path("create-user") {
        entity(as[User]) { user =>
          val saved: Future[Int] = baseRepository.save[User, UserTable](user, Tables.users)
          onSuccess(saved) { _ =>
            complete("user created")
          }
        }
      }
    },
    delete {
      path("delete-user") {
        parameters("id") { id =>
          val deleted = baseRepository.delete[User, UserTable](Tables.users.filter(_.id === id.toInt))
          onSuccess(deleted) { _ =>
            complete("user deleted")
          }
        }
      }
    },
    post {
      path("update-user") {
        entity(as[User]) { user =>
          val updated = baseRepository.update[User, UserTable](user, Tables.users)
          onSuccess(updated) { _ =>
            complete("user updated")
          }
        }
      }
    },
    get {
      path("find-user") {
        parameters("id") { id =>
          val user = baseRepository.find[User, UserTable](Tables.users.filter(_.id === id.toInt))
          val userGroupRelations = user.map(user => userGroupRepository.findRelationsForUser(user))
          val userGroups = userGroupRelations.flatMap(ugr => groupRepository.findUserGroups(ugr))
          val result = user.flatMap(user => userGroups.map(groups => mapToResultUser(user, groups)))
          onSuccess(result) { result =>
            complete(result)
          }
        }
      }
    }
  )

  def mapToResultUser(user: User, groups: Seq[Group]): ResultUser = {
    ResultUser(
      user.id,
      user.firstName,
      user.lastName,
      user.dob,
      user.email,
      user.password,
      groups
    )
  }
}
