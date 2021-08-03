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

  def create(groupDto: GroupDto): Future[Int] = baseRepository.save[Group, GroupTable](GroupMapper.groupDtoToGroup(groupDto), Tables.groups)

  def delete(id: Int): Future[Int] = baseRepository.delete[Group, GroupTable](Tables.groups.filter(_.id === id))

  def update(groupDto: GroupDto): Future[Int] = baseRepository.update[Group, GroupTable](GroupMapper.groupDtoToGroup(groupDto), Tables.groups)

  def findById(id: Int): Future[Option[ResultGroup]] = {
    val group = baseRepository.find[Group, GroupTable](Tables.groups.filter(_.id === id))
    val groupUsersRelations = group.map(group => group.map(userGroupRepository.findRelationsForGroup))
    val groupUsers = groupUsersRelations.flatMap(ugr => Future.sequence(Option.option2Iterable(ugr.map(userRepository.findGroupUsers))).map(_.head))
    group.flatMap(group => groupUsers.map(users => group.map(g => GroupMapper.mapToResultGroup(g, users))))
  }
}

object GroupMapper {
  def mapToResultGroup(group: Group, users: Seq[User]): ResultGroup = {
    ResultGroup(
      group.id,
      group.displayName,
      users.map(user => UserMapper.userToUserDto(user))
    )
  }

  def groupToGroupDto(group: Group): GroupDto = {
    GroupDto(
      group.id,
      group.displayName,
    )
  }

  def groupDtoToGroup(groupDto: GroupDto): Group = {
    Group(
      groupDto.id,
      groupDto.displayName,
    )
  }
}