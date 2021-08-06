package service

import entity.{GroupDto, GroupUsers, UserDto, UserGroup}
import repository.{DatabaseStorage, GroupRepository, UserGroupRepository, UserRepository}
import slick.jdbc.H2Profile.api._

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GroupUsersService(
                         groupRepository: GroupRepository = new GroupRepository,
                         userGroupRepository: UserGroupRepository = new UserGroupRepository,
                         userRepository: UserRepository = new UserRepository
                       ) {
  def save(groupUsers: GroupUsers): Future[Either[String, String]] = {
    val action = groupRepository.saveReturningId(GroupMapper.groupDtoToGroup(groupUsers.group)).flatMap(groupId =>
      userRepository.saveAllReturningIds(groupUsers.users.map(UserMapper.userDtoToUser)).map(usersIds => {
        usersIds.map(userId =>
          UserGroup(0, userId, groupId)
        )
      }).flatMap(userGroupRepository.saveAll)
    ).transactionally
    DatabaseStorage.db.run(action).map(res => Option.option2Iterable(res).head).map(r => {
      if (r == 0)
        Left("Fail")
      else
        Right("Success")
    })
  }

  def generateGroupUsers(): Future[List[Either[String, String]]] = {
    Future.sequence(
      (30000 until 60000).map(i => GroupUsers(GroupDto(0, s"GroupName$i"), (0 until 20).map(j => UserDto(
        i + j,
        s"firstName$i$j",
        s"lastName$i$j",
        LocalDate.now(),
        s"email$i$j@mail.ru",
        s"pass$i$j"
      )).toList
      )).map(save).toList)
  }
}