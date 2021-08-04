package entity

case class GroupUsers(
                       group: GroupDto,
                       users: List[UserDto]
                     )