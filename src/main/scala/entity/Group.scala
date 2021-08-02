package entity

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{Rep, Tag}

import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

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

class GroupRepository(db: Database = Database.forConfig("postgres")) {

  def isGroupWithNameExists(displayName: String): Future[Boolean] = {
    db.run(Tables.groups.filter(_.displayName === displayName).exists.result)
  }

  def save(group: Group): Future[Int] = {
    val trySave = Try {
      val exists = isGroupWithNameExists(group.displayName)
      exists.flatMap(res => {
        if (!res) {
          val insertGroupQuery = Tables.groups += group
          db.run(insertGroupQuery)
        } else {
          Future(0)
        }
      })
    }
    trySave match {
      case Success(v) => v
      case Failure(_) => Future.successful(0)
    }
  }

  def find(displayName: String): Future[Group] = {
    val tryFind = Try {
      val findQuery = Tables.groups.filter(_.displayName === displayName).result
      db.run(findQuery).map(_.head)
    }
    tryFind match {
      case Success(v) => v
    }
  }

  def readAll(): Future[Seq[Group]] = {
    db.run[Seq[Group]](Tables.groups.result)
  }

  def findUserGroups(groupsId: Query[Rep[Int], Int, Seq]): Future[Seq[Group]] = {
    db.run[Seq[Group]](Tables.groups.filter(_.id in groupsId).result)
  }

  def update(displayName: String, newDisplayName: String) = {
    val tryUpdate = Try {
      val updateQuery = Tables.groups.filter(_.displayName === displayName).map(_.displayName).update(newDisplayName)
      db.run(updateQuery)
    }
    tryUpdate match {
      case Success(_) => Future.successful(1)
      case Failure(e) => print(e)
        Future.successful(0)
    }
  }

  def delete(displayName: String): Future[Int] = {
    val tryDelete = Try {
      val deleteQuery = Tables.groups.filter(_.displayName === displayName).delete
      db.run(deleteQuery)
    }
    tryDelete match {
      case Success(v) => v
      case Failure(e) => print(e)
        Future.successful(0)
    }
  }
}