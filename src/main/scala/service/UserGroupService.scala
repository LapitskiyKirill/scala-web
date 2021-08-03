package service

import _root_.entity._
import repository.{BaseRepository, UserGroupRepository}
import slick.jdbc.PostgresProfile.api._
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserGroupService(
                        baseRepository: BaseRepository = new BaseRepository,
                        userGroupRepository: UserGroupRepository = new UserGroupRepository
                      ) {

  implicit val userGroupFormat: RootJsonFormat[UserGroup] = jsonFormat3(UserGroup)

  def addUserToGroup(userGroup: UserGroup): Future[Either[String, String]] = {
    checkIfUserAndGroupExists(userGroup).flatMap(exists => {
      baseRepository.save[UserGroup, UserGroupTable](userGroup, Tables.userGroup).map(res =>
        if (exists && res != 0)
          Right("Success")
        else
          Left("Fail")
      )
    })
  }

  def deleteUserFromGroup(userGroup: UserGroup): Future[Either[String, String]] = {
    checkIfUserAndGroupExists(userGroup).flatMap(exists => {
      userGroupRepository.removeUserFromGroup(userGroup.userId, userGroup.groupId).map(res =>
        if (exists && res != 0)
          Right("Success")
        else
          Left("Fail")
      )
    })
  }

  def checkIfUserAndGroupExists(userGroup: UserGroup): Future[Boolean] = {
    val user = baseRepository.exists[User, UserTable](Tables.users.filter(_.id === userGroup.userId))
    val group = baseRepository.exists[Group, GroupTable](Tables.groups.filter(_.id === userGroup.groupId))
    user.flatMap(u => {
      group.map(g => {
        g && u
      })
    })
  }
}