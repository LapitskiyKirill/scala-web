package entity

import _root_.entity.UserImplicits.localDateFormat
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

object Implicits {
  implicit val userFormat: RootJsonFormat[UserDto] = jsonFormat6(UserDto)
  implicit val groupFormat: RootJsonFormat[GroupDto] = jsonFormat2(GroupDto)
  implicit val userGroupFormat: RootJsonFormat[UserGroup] = jsonFormat3(UserGroup)
  implicit val resultUserFormat: RootJsonFormat[ResultUser] = jsonFormat7(ResultUser)
  implicit val resultGroupFormat: RootJsonFormat[ResultGroup] = jsonFormat3(ResultGroup)
  implicit val responseFormat: RootJsonFormat[Response] = jsonFormat1(Response)
  implicit val groupUsersFormat: RootJsonFormat[GroupUsers] = jsonFormat2(GroupUsers)
}