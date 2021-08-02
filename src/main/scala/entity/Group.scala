package entity

import slick.jdbc.PostgresProfile.api._
import slick.lifted.{Rep, Tag}

import scala.collection.immutable.Seq

case class Group(
                  id: Int,
                  displayName: String
                )

case class ResultGroup(
                        id: Int,
                        displayName: String,
                        users: Seq[User]
                      )

class GroupTable(tag: Tag) extends Table[Group](tag, "group") {
  def * = (id, displayName) <> (Group.tupled, Group.unapply)

  val id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)
  val displayName: Rep[String] = column[String]("display_name")
}