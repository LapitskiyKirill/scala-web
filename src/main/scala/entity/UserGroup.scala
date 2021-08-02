package entity

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{Rep, Tag}

import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

case class UserGroup(id: Int, userId: Int, groupId: Int)

case class UserGroupRequest(userEmail: String, groupDisplayName: String)

class UserGroupTable(tag: Tag) extends Table[UserGroup](tag, "user_group") {
  def * = (id, userId, groupId) <> (UserGroup.tupled, UserGroup.unapply)

  val id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)
  val userId: Rep[Int] = column[Int]("user_id")
  val groupId: Rep[Int] = column[Int]("group_id")

  def userIdFk = foreignKey("user_group_user_id_fk", userId, Tables.users)(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)

  def groupIdFk = foreignKey("user_group_group_id_fk", groupId, Tables.groups)(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
}

class UserGroupRepository(db: Database = Database.forConfig("postgres")) {

  def isUserWithEmailExists(email: String): Future[Boolean] = {
    db.run(Tables.users.filter(_.email === email).exists.result)
  }

  def save(userGroup: UserGroup): Future[Int] = {
    val trySave = Try {
      val insertRelationQuery = Tables.userGroup += userGroup
      db.run(insertRelationQuery)
    }
    trySave match {
      case Success(v) => v
      case Failure(_) => Future.successful(0)
    }
  }

  def readAll(): Future[Seq[User]] = {
    db.run[Seq[User]](Tables.users.result)
  }

  def find(user: User, group: Group): Future[UserGroup] = {
    val tryFind = Try {
      val findQuery = Tables.userGroup.filter(userGroup => userGroup.userId === user.id && userGroup.groupId === group.id).result
      db.run(findQuery).map(_.head)
    }
    tryFind match {
      case Success(v) => v
      case Failure(e) =>
        println(e)
        Future.successful(UserGroup(0, 0, 0))
    }
  }

  def addUserToGroup(user: User, group: Group): Future[Int] = {
    val tryAdd = Try {
      val findQuery = Tables.userGroup += UserGroup(0, user.id, group.id)
      db.run(findQuery)
    }
    tryAdd match {
      case Success(v) => v
      case Failure(e) =>
        println(e)
        Future.successful(0)
    }
  }

  def delete(user: User, group: Group): Future[Int] = {
    val tryDelete = Try {
      val findQuery = Tables.userGroup.filter(userGroup => userGroup.userId === user.id && userGroup.groupId === group.id).delete
      db.run(findQuery)
    }
    tryDelete match {
      case Success(v) => v
      case Failure(e) => print(e)
        Future.successful(0)
    }
  }

  def findRelationsForGroup(group: Group): Query[Rep[Int], Int, Seq] = {
    Tables.userGroup.filter(_.groupId === group.id).map(_.userId)
  }

  def findRelationsForUser(user: User): Query[Rep[Int], Int, Seq] = {
    Tables.userGroup.filter(_.userId === user.id).map(_.groupId)
  }
}