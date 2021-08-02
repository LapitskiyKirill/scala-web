package service

import _root_.entity._
import repository.{BaseRepository, GroupRepository, UserGroupRepository, UserRepository}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object UserService {

  val userRepository = new UserRepository
  val groupRepository = new GroupRepository
  val baseRepository = new BaseRepository
  val userGroupRepository = new UserGroupRepository

  def update(user: User): Future[Int] = baseRepository.update[User, UserTable](user, Tables.users)

  def create(user: User): Future[Int] = baseRepository.save[User, UserTable](user, Tables.users)

  def deleteById(id: Int): Future[Int] = baseRepository.delete[User, UserTable](Tables.users.filter(_.id === id))

  def findById(id: Int): Future[ResultUser] = {
    val user = baseRepository.find[User, UserTable](Tables.users.filter(_.id === id))
    val userGroupRelations = user.map(user => userGroupRepository.findRelationsForUser(user))
    val userGroups = userGroupRelations.flatMap(ugr => groupRepository.findUserGroups(ugr))
    user.flatMap(user => userGroups.map(groups => mapToResultUser(user, groups)))
  }

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
