package repository

import entity.{Group, Tables, User, UserGroup}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Rep

import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class UserGroupRepository(db: Database = DatabaseStorage.db) {

  def saveAll(userGroups: Seq[UserGroup]): DBIO[Option[Int]] = {
    Tables.userGroup ++= userGroups
  }

  def removeUserFromGroup(userId: Int, groupId: Int): Future[Int] = {
    val tryAdd = Try {
      val removeQuery = Tables.userGroup.filter(ug => ug.userId === userId && ug.groupId === groupId).delete
      db.run(removeQuery)
    }
    tryAdd match {
      case Success(v) => v
      case Failure(e) =>
        println(e)
        Future.successful(0)
    }
  }

  def findRelationsForGroup(group: Group): Query[Rep[Int], Int, Seq] = {
    Tables.userGroup.filter(_.groupId === group.id).map(_.userId)
  }

  def findRelationsForUser(user: User): Query[Rep[Int], Int, Seq] = {
    Tables.userGroup.filter(_.userId === user.id).map(_.groupId)
  }

  def checkIfUserAndGroupExists(userGroup: UserGroup): Future[(Boolean, Boolean)] = {
    db.run((for {
      (c, s) <- Tables.users join Tables.groups
    } yield (c.id === userGroup.userId, s.id === userGroup.groupId)).result.map(_.head))
  }
}