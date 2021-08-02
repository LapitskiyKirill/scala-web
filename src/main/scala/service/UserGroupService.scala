package service

import _root_.entity._
import repository.BaseRepository
import slick.jdbc.PostgresProfile.api._
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object UserGroupService {

  val baseRepository = new BaseRepository
  implicit val userGroupFormat: RootJsonFormat[UserGroup] = jsonFormat3(UserGroup)

  def addUserToGroup(userGroup: UserGroup): Future[Either[String, Future[Int]]] = {
    checkIfUserAndGroupExists(userGroup).map(exists => {
      if (exists)
        Right(baseRepository.save[UserGroup, UserGroupTable](userGroup, Tables.userGroup))
      else
        Left("fail")
    })
  }

  def deleteUserFromGroup(userGroup: UserGroup): Future[Either[String, Future[Int]]] = {
    checkIfUserAndGroupExists(userGroup).map(exists => {
      if (exists)
        Right(baseRepository.delete[UserGroup, UserGroupTable](Tables.userGroup.filter(_.id === userGroup.id)))
      else
        Left("fail")
    })
  }

  def checkIfUserAndGroupExists(userGroup: UserGroup): Future[Boolean] = {
    val user = baseRepository.exists[User, UserTable](Tables.users.filter(_.id === userGroup.userId))
    val group = baseRepository.exists[Group, GroupTable](Tables.groups.filter(_.id === userGroup.groupId))
    user.flatMap(u => {
      group.map(g => {
        if (g && u)
          true
        else
          false
      })
    })
  }

}