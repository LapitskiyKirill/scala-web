package service

import _root_.entity._
import akka.NotUsed
import akka.stream.scaladsl.Source
import repository.{BaseRepository, DatabaseStorage, GroupRepository, UserGroupRepository}
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.{ResultSetConcurrency, ResultSetType}


import java.sql.Date
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class UserService(
                   groupRepository: GroupRepository = new GroupRepository,
                   baseRepository: BaseRepository = new BaseRepository,
                   userGroupRepository: UserGroupRepository = new UserGroupRepository
                 ) {


  def update(id: Int, userDto: UserDto): Future[Either[String, String]] =
    baseRepository.update[User, UserTable](UserMapper.userDtoToUser(userDto), Tables.users.filter(_.id === id)).map(res =>
      if (res != 0)
        Right("Success")
      else
        Left("Fail")
    )

  def create(userDto: UserDto): Future[Either[String, String]] =
    baseRepository.save[User, UserTable](UserMapper.userDtoToUser(userDto), Tables.users).map(res =>
      if (res != 0)
        Right("Success")
      else
        Left("Fail")
    )

  def deleteById(id: Int): Future[Either[String, String]] =
    baseRepository.delete[User, UserTable](Tables.users.filter(_.id === id)).map(res =>
      if (res != 0)
        Right("Success")
      else
        Left("Fail")
    )

  def findById(id: Int): Future[Either[String, Option[ResultUser]]] = {
    val user = baseRepository.find[User, UserTable](Tables.users.filter(_.id === id))
    val userGroupRelations = user.map(_.map(userGroupRepository.findRelationsForUser))
    val userGroups = userGroupRelations.flatMap(ugr => Future.sequence(Option.option2Iterable(ugr.map(groupRepository.findUserGroups))).map(_.head))
    user.flatMap(user => userGroups.map(groups => user.map(u => UserMapper.mapToResultUser(u, groups)))).map(res =>
      if (res.isDefined)
        Right(res)
      else
        Left("Fail")
    )
  }

  def getAll = {

    val join = Tables.users.join(Tables.userGroup).on(_.id === _.userId).result.withStatementParameters(
      rsType = ResultSetType.ScrollSensitive,
      rsConcurrency = ResultSetConcurrency.Auto,
      fetchSize = 1
    )
    Source.fromPublisher(DatabaseStorage.db.stream(join)).throttle(10, 1.second)
    //    val user = Tables.users.take(1000).result.withStatementParameters(
    //      rsType = ResultSetType.ScrollSensitive,
    //      rsConcurrency = ResultSetConcurrency.Auto,
    //      fetchSize = 1
    //    )
    //    Source.fromPublisher(DatabaseStorage.db.stream(user)).throttle(10, 1.second)

    //    val res = DatabaseStorage.db.run(user)
    //      .flatMap(a =>
    //        Future.sequence(a.map(user =>
    //          groupRepository.findUserGroups(userGroupRepository.findRelationsForUser(user))
    //            .map(groupSeq => {
    //              val resUser = UserMapper.mapToResultUser(user, groupSeq)
    //              println(resUser)
    //              resUser
    //            }
    //            )
    //        ))
    //      )
    //    Future(true)
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