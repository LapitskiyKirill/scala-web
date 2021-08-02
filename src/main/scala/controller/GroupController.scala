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

class GroupController {
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
      path("create-group") {
        entity(as[Group]) { group =>
          val saved: Future[Int] = baseRepository.save[Group, GroupTable](group, Tables.groups)
          onSuccess(saved) { _ =>
            complete("group created")
          }
        }
      }
    },
    delete {
      path("delete-group") {
        parameters("id") { id =>
          val deleted = baseRepository.delete[Group, GroupTable](Tables.groups.filter(_.id === id.toInt))
          onSuccess(deleted) { _ =>
            complete("group deleted")
          }
        }
      }
    },
    post {
      path("update-group") {
        entity(as[Group]) { group =>
          val updated = baseRepository.update[Group, GroupTable](group, Tables.groups)
          onSuccess(updated) { _ =>
            complete("group updated")
          }
        }
      }
    },
    get {
      path("find-group") {
        parameters("id") { id =>
          val group = baseRepository.find[Group, GroupTable](Tables.groups.filter(_.id === id.toInt))
          val groupUsersRelations = group.map(group => userGroupRepository.findRelationsForGroup(group))
          val groupUsers = groupUsersRelations.flatMap(ugr => userRepository.findGroupUsers(ugr))
          val result = group.flatMap(group => groupUsers.map(users => mapToResultGroup(group, users)))
          onSuccess(result) { result =>
            complete(result)
          }
        }
      }
    }
  )

  def mapToResultGroup(group: Group, users: Seq[User]) = {
    ResultGroup(
      group.id,
      group.displayName,
      users
    )
  }
}
