package service

import _root_.entity._
import repository.{BaseRepository, UserGroupRepository, UserRepository}
import slick.jdbc.PostgresProfile.api._

import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GroupService(
                    userRepository: UserRepository = new UserRepository,
                    baseRepository: BaseRepository = new BaseRepository,
                    userGroupRepository: UserGroupRepository = new UserGroupRepository
                  ) {

  def create(groupDto: GroupDto): Future[Either[String, String]] =
    baseRepository.save[Group, GroupTable](GroupMapper.groupDtoToGroup(groupDto), Tables.groups).map(res =>
      if (res != 0)
        Right("Success")
      else
        Left("Fail")
    )

  def delete(id: Int): Future[Either[String, String]] =
    baseRepository.delete[Group, GroupTable](Tables.groups.filter(_.id === id)).map(res =>
      if (res != 0)
        Right("Success")
      else
        Left("Fail")
    )

  def update(id: Int, groupDto: GroupDto): Future[Either[String, String]] =
    baseRepository.update[Group, GroupTable](GroupMapper.groupDtoToGroup(groupDto), Tables.groups.filter(_.id === id)).map(res =>
      if (res != 0)
        Right("Success")
      else
        Left("Fail")
    )

  def findById(id: Int): Future[Either[String, Option[ResultGroup]]] = {
    val group = baseRepository.find[Group, GroupTable](Tables.groups.filter(_.id === id))
    val groupUsersRelations = group.map(group => group.map(userGroupRepository.findRelationsForGroup))
    val groupUsers = groupUsersRelations.flatMap(ugr => Future.sequence(Option.option2Iterable(ugr.map(userRepository.findGroupUsers))).map(_.head))
    group.flatMap(group => groupUsers.map(users => group.map(g => GroupMapper.mapToResultGroup(g, users)))).map(res =>
      if (res.isDefined)
        Right(res)
      else
        Left("Fail")
    )
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