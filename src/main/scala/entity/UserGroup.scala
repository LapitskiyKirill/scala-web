package entity

import slick.jdbc.PostgresProfile.api._
import slick.lifted.{Rep, Tag}

case class UserGroup(id: Int, userId: Int, groupId: Int)

class UserGroupTable(tag: Tag) extends Table[UserGroup](tag, "user_group") {
  def * = (id, userId, groupId) <> (UserGroup.tupled, UserGroup.unapply)

  val id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)
  val userId: Rep[Int] = column[Int]("user_id")
  val groupId: Rep[Int] = column[Int]("group_id")

  def userIdFk = foreignKey("user_group_user_id_fk", userId, Tables.users)(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)

  def groupIdFk = foreignKey("user_group_group_id_fk", groupId, Tables.groups)(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
}