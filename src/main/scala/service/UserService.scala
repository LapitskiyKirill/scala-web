package service

import _root_.entity._
import repository.{BaseRepository, GroupRepository, UserGroupRepository, UserRepository}
import slick.jdbc.PostgresProfile.api._

import java.sql.Date
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object UserService {

  val userRepository = new UserRepository
  val groupRepository = new GroupRepository
  val baseRepository = new BaseRepository
  val userGroupRepository = new UserGroupRepository

  def update(userDto: UserDto): Future[Int] =
    baseRepository.update[User, UserTable](UserMapper.userDtoToUser(userDto), Tables.users)

  def create(userDto: UserDto): Future[Int] =
    baseRepository.save[User, UserTable](UserMapper.userDtoToUser(userDto), Tables.users)

  def deleteById(id: Int): Future[Int] =
    baseRepository.delete[User, UserTable](Tables.users.filter(_.id === id))

  def findById(id: Int): Future[Option[ResultUser]] = {
    val user = baseRepository.find[User, UserTable](Tables.users.filter(_.id === id))
    val userGroupRelations = user.map(_.map(userGroupRepository.findRelationsForUser))
    val userGroups = userGroupRelations.flatMap(ugr => Future.sequence(Option.option2Iterable(ugr.map(groupRepository.findUserGroups))).map(_.head))
    user.flatMap(user => userGroups.map(groups => user.map(u => UserMapper.mapToResultUser(u, groups))))
  }
}

object UserMapper {
  def mapToResultUser(user: User, groups: Seq[Group]): ResultUser = {
    ResultUser(
      user.id,
      user.firstName,
      user.lastName,
      user.dob.toLocalDate,
      user.email,
      user.password,
      groups.map(group => GroupMapper.groupToGroupDto(group))
    )
  }

  def userToUserDto(user: User): UserDto = {
    UserDto(
      user.id,
      user.firstName,
      user.lastName,
      user.dob.toLocalDate,
      user.email,
      user.password
    )
  }

  def userDtoToUser(userDto: UserDto): User = {
    User(
      userDto.id,
      userDto.firstName,
      userDto.lastName,
      Date.valueOf(userDto.dob),
      userDto.email,
      userDto.password
    )
  }
}