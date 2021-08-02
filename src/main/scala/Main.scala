import _root_.entity._
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives.{post, _}
import com.sun.org.apache.xml.internal.serializer.utils.Utils
import spray.json.DefaultJsonProtocol._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.StdIn

object Main {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem(Behaviors.empty, "my-system")
    val userRepository = new UserRepository
    val groupRepository = new GroupRepository
    val userGroupRepository = new UserGroupRepository
    implicit val userFormat = jsonFormat6(User)
    implicit val groupFormat = jsonFormat2(Group)
    implicit val userGroupRequestFormat = jsonFormat2(UserGroupRequest)
    implicit val userGroupFormat = jsonFormat3(UserGroup)
    implicit val resultUserFormat = jsonFormat7(ResultUser)
    implicit val resultGroupFormat = jsonFormat3(ResultGroup)

    val route = concat(
      post {
        path("create-user") {
          entity(as[User]) { user =>
            val saved: Future[Int] = userRepository.save(user)
            onSuccess(saved) { _ =>
              complete("order created")
            }
          }
        }
      },
      post {
        path("create-group") {
          entity(as[Group]) { group =>
            val saved: Future[Int] = groupRepository.save(group)
            onSuccess(saved) { _ =>
              complete("group created")
            }
          }
        }
      },
      post {
        path("add-user-to-group") {
          entity(as[UserGroupRequest]) { userGroupRequest =>
            val user: Future[User] = userRepository.find(userGroupRequest.userEmail)
            val group: Future[Group] = groupRepository.find(userGroupRequest.groupDisplayName)
            val saved: Future[Int] = user.flatMap(u => group.flatMap(g => userGroupRepository.addUserToGroup(u, g)))
            onSuccess(saved) { _ =>
              complete("user added to group")
            }
          }
        }
      },
      post {
        path("delete-user-from-group") {
          entity(as[UserGroupRequest]) { userGroupRequest =>
            val user = userRepository.find(userGroupRequest.userEmail)
            val group = groupRepository.find(userGroupRequest.groupDisplayName)
            val deleted: Future[Int] = user.flatMap(u => group.flatMap(g => userGroupRepository.delete(u, g)))
            onSuccess(deleted) { _ =>
              complete("user deleted from group")
            }
          }
        }
      },
      delete {
        path("delete-user") {
          parameters("userEmail") { userEmail =>
            val deleted = userRepository.delete(userEmail)
            onSuccess(deleted) { _ =>
              complete("user deleted")
            }
          }
        }
      },
      delete {
        path("delete-group") {
          parameters("displayName") { displayName =>
            val deleted = groupRepository.delete(displayName)
            onSuccess(deleted) { _ =>
              complete("group deleted")
            }
          }
        }
      },
      post {
        path("update-user") {
          entity(as[User]) { user =>
            val updated = userRepository.update(user)
            onSuccess(updated) { _ =>
              complete("user updated")
            }
          }
        }
      },
      post {
        path("update-group") {
          parameters("displayName", "newDisplayName") { (displayName, newDisplayName) =>
            val updated = groupRepository.update(displayName, newDisplayName)
            onSuccess(updated) { _ =>
              complete("group updated")
            }
          }
        }
      },
      get {
        path("find-user") {
          parameters("email") { email =>
            val user = userRepository.find(email)
            val userGroupRelations = user.map(user => userGroupRepository.findRelationsForUser(user))
            val userGroups = userGroupRelations.flatMap(ugr => groupRepository.findUserGroups(ugr))
            val result = user.flatMap(user => userGroups.map(groups => UtilMapper.mapToResultUser(user, groups)))
            onSuccess(result) { result =>
              complete(result)
            }
          }
        }
      },
      get {
        path("find-group") {
          parameters("displayName") { displayName =>
            val group = groupRepository.find(displayName)
            val groupUsersRelations = group.map(group => userGroupRepository.findRelationsForGroup(group))
            val groupUsers = groupUsersRelations.flatMap(ugr => userRepository.findGroupUsers(ugr))
            val result = group.flatMap(group => groupUsers.map(users => UtilMapper.mapToResultGroup(group, users)))
            onSuccess(result) { result =>
              complete(result)
            }
          }
        }
      }
    )

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)

    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}