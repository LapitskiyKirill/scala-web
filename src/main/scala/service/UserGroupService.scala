package service

import _root_.entity._
import repository.{BaseRepository, UserGroupRepository}
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserGroupService(
                        baseRepository: BaseRepository = new BaseRepository,
                        userGroupRepository: UserGroupRepository = new UserGroupRepository
                      ) {

  implicit val userGroupFormat: RootJsonFormat[UserGroup] = jsonFormat3(UserGroup)

  def addUserToGroup(userId: Int, groupId: Int): Future[Either[String, String]] = {
    baseRepository.save[UserGroup, UserGroupTable](UserGroup(0, userId, groupId), Tables.userGroup).map(res =>
      if (res != 0) {
        Right("Success")
      } else
        Left("Fail")
    )
  }

  def deleteUserFromGroup(userId: Int, groupId: Int): Future[Either[String, String]] = {
    val userGroup = UserGroup(0, userId, groupId)
      userGroupRepository.removeUserFromGroup(userGroup.userId, userGroup.groupId).map(res =>
        if (res != 0)
          Right("Success")
        else
          Left("Fail")
      )
  }

  def checkIfUserAndGroupExists(userGroup: UserGroup): Future[Boolean] = {
    userGroupRepository.checkIfUserAndGroupExists(userGroup).map(tuple => tuple._1 && tuple._2)
  }
}