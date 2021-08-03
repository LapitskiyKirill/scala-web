package repository

import entity.{Group, Tables, User}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Rep

import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class BaseRepository(db: Database = Database.forConfig("postgres")) {
  def save[O, T <: Table[O]](obj: O, table: TableQuery[T]): Future[Int] = {
    val trySave = Try {
      val insertGroupQuery = table += obj
      db.run(insertGroupQuery)
    }
    trySave match {
      case Success(v) => v
      case Failure(_) => Future.successful(0)
    }
  }

  def find[O, T <: Table[O]](findBy: Query[T, T#TableElementType, Seq]): Future[Option[O]] = {
    val tryFind = Try {
      val findQuery = findBy.result
      db.run(findQuery).map(_.head)
    }
    tryFind match {
      case Success(v) => v.map(value => Option(value))
      case Failure(e) => e.printStackTrace()
        Future(Option.empty[O])
    }
  }

  def update[O, T <: Table[O]](obj: O, findBy: Query[T, T#TableElementType, Seq]): Future[Int] = {
    val tryUpdate = Try {
      val updateQuery = findBy.update(obj)
      db.run(updateQuery)
    }
    tryUpdate match {
      case Success(_) => Future.successful(1)
      case Failure(e) => print(e)
        Future.successful(0)
    }
  }

  def delete[O, T <: Table[O]](findBy: Query[T, T#TableElementType, Seq]): Future[Int] = {
    val tryDelete = Try {
      val deleteQuery = findBy.delete
      db.run(deleteQuery)
    }
    tryDelete match {
      case Success(v) => v
      case Failure(e) => print(e)
        Future.successful(0)
    }
  }

  def exists[O, T <: Table[O]](findBy: Query[T, T#TableElementType, Seq]): Future[Boolean] = {
    db.run(findBy.exists.result)
  }
}

class UserGroupRepository(db: Database = Database.forConfig("postgres")) {

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
}

class UserRepository(db: Database = Database.forConfig("postgres")) {

  def findGroupUsers(usersId: Query[Rep[Int], Int, Seq]): Future[Seq[User]] = {
    db.run[Seq[User]](Tables.users.filter(_.id in usersId).result)
  }
}

class GroupRepository(db: Database = Database.forConfig("postgres")) {

  def findUserGroups(groupsId: Query[Rep[Int], Int, Seq]): Future[Seq[Group]] = {
    db.run[Seq[Group]](Tables.groups.filter(_.id in groupsId).result)
  }
}