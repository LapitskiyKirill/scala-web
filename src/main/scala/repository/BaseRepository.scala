package repository

import entity.{Group, Tables, User, UserGroup}
import slick.jdbc.JdbcBackend
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Rep

import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object DatabaseStorage {
  def db: JdbcBackend.Database = Database.forConfig("postgres")
}

class BaseRepository(db: Database = DatabaseStorage.db) {
  def save[O, T <: Table[O]](obj: O, table: TableQuery[T]): Future[Int] = {
    processResult(Try {
      val insertGroupQuery = table += obj
      db.run(insertGroupQuery)
    })
  }

  def saveAll[O, T <: Table[O]](objs: Seq[O], table: TableQuery[T]): Future[Option[Int]] = {
    val trySave = Try {
      val insertGroupQuery = table ++= objs
      db.run(insertGroupQuery).map(a => a.map(b => b))
    }
    trySave match {
      case Success(v) => v
      case Failure(e) => e.printStackTrace()
        Future.successful(Option(0))
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
        Future.successful(Option.empty[O])
    }
  }

  def update[O, T <: Table[O]](obj: O, findBy: Query[T, T#TableElementType, Seq]): Future[Int] = {
    processResult(Try {
      val updateQuery = findBy.update(obj)
      db.run(updateQuery)
    })
  }

  def delete[O, T <: Table[O]](findBy: Query[T, T#TableElementType, Seq]): Future[Int] = {
    processResult(Try {
      val deleteQuery = findBy.delete
      db.run(deleteQuery)
    })
  }

  def processResult(result: Try[Future[Int]]): Future[Int] = {
    result match {
      case Success(v) => v
      case Failure(e) => print(e)
        Future.successful(0)
    }
  }

  def exists[O, T <: Table[O]](findBy: Query[T, T#TableElementType, Seq]): Future[Boolean] = {
    db.run(findBy.exists.result)
  }
}

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

class UserRepository(db: Database = DatabaseStorage.db) {
  def saveAllReturningIds(users: List[User]): DBIO[Seq[Int]] = { //Future[Seq[Int]] = {
    //    db.run(Tables.users returning Tables.users.map(_.id) ++= users)
    Tables.users returning Tables.users.map(_.id) ++= users
  }

  def findGroupUsers(usersId: Query[Rep[Int], Int, Seq]): Future[Seq[User]] = {
    db.run[Seq[User]](Tables.users.filter(_.id in usersId).result)
  }
}

class GroupRepository(db: Database = DatabaseStorage.db) {
  def saveReturningId(group: Group): DBIO[Int] = {
    //    db.run(Tables.groups returning Tables.groups.map(_.id) += group)
    Tables.groups returning Tables.groups.map(_.id) += group
  }

  def findUserGroups(groupsId: Query[Rep[Int], Int, Seq]): Future[Seq[Group]] = {
    db.run[Seq[Group]](Tables.groups.filter(_.id in groupsId).result)
  }
}