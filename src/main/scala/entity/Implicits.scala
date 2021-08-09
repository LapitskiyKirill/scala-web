package entity

import _root_.entity.UserImplicits.localDateFormat
import spray.json.DefaultJsonProtocol._
import spray.json.{JsString, JsValue, RootJsonFormat}

import java.sql.Date

object Implicits {
  implicit val userFormat: RootJsonFormat[UserDto] = jsonFormat6(UserDto)
  implicit val user1Format: RootJsonFormat[User] = jsonFormat6(User)
  implicit val groupFormat: RootJsonFormat[GroupDto] = jsonFormat2(GroupDto)
  implicit val userGroupFormat: RootJsonFormat[UserGroup] = jsonFormat3(UserGroup)
  implicit val resultUserFormat: RootJsonFormat[ResultUser] = jsonFormat7(ResultUser)
  implicit val resultGroupFormat: RootJsonFormat[ResultGroup] = jsonFormat3(ResultGroup)
  implicit val responseFormat: RootJsonFormat[Response] = jsonFormat1(Response)
  implicit val groupUsersFormat: RootJsonFormat[GroupUsers] = jsonFormat2(GroupUsers)
  implicit val dateFormat: RootJsonFormat[Date] = new RootJsonFormat[Date] {
    def write(x: Date) = JsString(x.toString)

    def read(value: JsValue): Date = value match {
      case JsString(x) => Date.valueOf(value.toString())
      case x => throw new RuntimeException(s"Unexpected type ${x.getClass.getName} when trying to parse LocalDateTime")
    }
  }
}