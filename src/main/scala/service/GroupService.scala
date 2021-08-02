package service

import _root_.entity._
import repository.{BaseRepository, GroupRepository, UserGroupRepository, UserRepository}
import slick.jdbc.PostgresProfile.api._

import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object GroupService {
  val userRepository = new UserRepository
  val groupRepository = new GroupRepository
  val baseRepository = new BaseRepository
  val userGroupRepository = new UserGroupRepository

  def create(group: Group): Future[Int] = baseRepository.save[Group, GroupTable](group, Tables.groups)

  def delete(id: Int): Future[Int] = baseRepository.delete[Group, GroupTable](Tables.groups.filter(_.id === id))

  def update(group: Group): Future[Int] = baseRepository.update[Group, GroupTable](group, Tables.groups)

  def findById(id: Int): Future[ResultGroup] = {
    val group = baseRepository.find[Group, GroupTable](Tables.groups.filter(_.id === id))
    val groupUsersRelations = group.map(group => userGroupRepository.findRelationsForGroup(group))
    val groupUsers = groupUsersRelations.flatMap(ugr => userRepository.findGroupUsers(ugr))
    group.flatMap(group => groupUsers.map(users => mapToResultGroup(group, users)))
  }

  def mapToResultGroup(group: Group, users: Seq[User]): ResultGroup = {
    ResultGroup(
      group.id,
      group.displayName,
      users
    )
  }
}
