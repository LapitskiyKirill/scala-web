package entity

import slick.jdbc.PostgresProfile.api._
import slick.lifted.{Rep, Tag}
import spray.json.{JsString, JsValue, RootJsonFormat}

import java.sql.Date
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.collection.immutable.Seq

case class User(
                 id: Int,
                 firstName: String,
                 lastName: String,
                 dob: Date,
                 email: String,
                 password: String
               )

case class UserDto(
                    id: Int,
                    firstName: String,
                    lastName: String,
                    dob: LocalDate,
                    email: String,
                    password: String
                  )

final case class ResultUser(
                             id: Int,
                             firstName: String,
                             lastName: String,
                             dob: LocalDate,
                             email: String,
                             password: String,
                             groups: Seq[GroupDto]
                           )

class UserTable(tag: Tag) extends Table[User](tag, "user") {
  def * = (id, firstName, lastName, dob, email, password) <> (User.tupled, User.unapply)

  val id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)
  val firstName: Rep[String] = column[String]("first_name")
  val lastName: Rep[String] = column[String]("last_name")
  val dob: Rep[Date] = column[Date]("dob")
  val email: Rep[String] = column[String]("email")
  val password: Rep[String] = column[String]("password")

  val groups = Tables.userGroup.filter(_.userId === id)
}

object UserImplicits {
  implicit val localDateFormat: RootJsonFormat[LocalDate] = new RootJsonFormat[LocalDate] {
    private val iso_date_time = DateTimeFormatter.ISO_DATE

    def write(x: LocalDate): JsString = JsString(iso_date_time.format(x))

    def read(value: JsValue): LocalDate = value match {
      case JsString(x) => LocalDate.parse(x, iso_date_time)
      case x => throw new RuntimeException(s"Unexpected type ${x.getClass.getName} when trying to parse LocalDateTime")
    }
  }
  implicit val dateFormat: RootJsonFormat[Date] = new RootJsonFormat[Date] {
    def write(x: Date): JsString = JsString(x.toString)

    def read(value: JsValue): Date = value match {
      case JsString(x) => Date.valueOf(value.toString())
      case x => throw new RuntimeException(s"Unexpected type ${x.getClass.getName} when trying to parse Date")
    }
  }
}