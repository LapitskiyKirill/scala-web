package entity

import slick.jdbc.PostgresProfile.api._
import slick.lifted.{Rep, Tag}

import scala.collection.immutable.Seq

case class User(
                 id: Int,
                 firstName: String,
                 lastName: String,
                 dob: String,
                 email: String,
                 password: String
               )

case class ResultUser(
                       id: Int,
                       firstName: String,
                       lastName: String,
                       dob: String,
                       email: String,
                       password: String,
                       groups: Seq[Group]
                     )

class UserTable(tag: Tag) extends Table[User](tag, "user") {
  def * = (id, firstName, lastName, dob, email, password) <> (User.tupled, User.unapply)

  val id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)
  val firstName: Rep[String] = column[String]("first_name")
  val lastName: Rep[String] = column[String]("last_name")
  val dob: Rep[String] = column[String]("dob")
  val email: Rep[String] = column[String]("email")
  val password: Rep[String] = column[String]("password")

  val groups = Tables.userGroup.filter(_.userId === id)
}