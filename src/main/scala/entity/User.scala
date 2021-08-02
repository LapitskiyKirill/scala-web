package entity

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{Rep, Tag}

import scala.List
import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

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

class UserRepository(db: Database = Database.forConfig("postgres")) {

  def isUserWithEmailExists(email: String): Future[Boolean] = {
    db.run(Tables.users.filter(_.email === email).exists.result)
  }

  def save(user: User): Future[Int] = {
    val trySave = Try {
      val exists = isUserWithEmailExists(user.email)
      exists.flatMap(res => {
        if (!res) {
          val insertUserQuery = Tables.users += user
          db.run(insertUserQuery)
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

  def find(userEmail: String): Future[User] = {
    val tryFind = Try {
      val findQuery = Tables.users.filter(_.email === userEmail).result
      db.run(findQuery).map(_.head)
    }
    tryFind match {
      case Success(v) => v
    }
  }

  def readAll(): Future[Seq[User]] = {
    db.run[Seq[User]](Tables.users.result)
  }

  def findGroupUsers(usersId: Query[Rep[Int], Int, Seq]): Future[Seq[User]] = {
    db.run[Seq[User]](Tables.users.filter(_.id in usersId).result)
  }

  def update(user: User): Future[Int] = {
    val tryUpdate = Try {
      val updateObject = Tables.users.filter(_.email === user.email)
      val updateQuery = DBIO.seq(
        updateObject.map(_.firstName).update(user.firstName),
        updateObject.map(_.lastName).update(user.lastName),
        updateObject.map(_.dob).update(user.dob),
        updateObject.map(_.password).update(user.password)
      )
      db.run(updateQuery)
    }
    tryUpdate match {
      case Success(_) => Future.successful(1)
      case Failure(e) => print(e)
        Future.successful(0)
    }
  }

  def delete(userEmail: String): Future[Int] = {
    val tryDelete = Try {
      val deleteQuery = Tables.users.filter(_.email === userEmail).delete
      db.run(deleteQuery)
    }
    tryDelete match {
      case Success(v) => v
      case Failure(e) => print(e)
        Future.successful(0)
    }
  }
}