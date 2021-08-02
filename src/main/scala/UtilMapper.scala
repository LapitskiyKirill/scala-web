import entity.{Group, ResultGroup, ResultUser, User}

import scala.collection.immutable.Seq

object UtilMapper {
  def mapToResultUser(user: User, groups: Seq[Group]) = {
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

  def mapToResultGroup(group: Group, users: Seq[User]) = {
    ResultGroup(
      group.id,
      group.displayName,
      users
    )
  }
}
