package repository

import entity.{Tables, User}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Rep

import scala.collection.immutable.Seq
import scala.concurrent.Future

class UserRepository(db: Database = DatabaseStorage.db) {
  def saveAllReturningIds(users: List[User]): DBIO[Seq[Int]] = {
    Tables.users returning Tables.users.map(_.id) ++= users
  }

  def findGroupUsers(usersId: Query[Rep[Int], Int, Seq]): Future[Seq[User]] = {
    db.run[Seq[User]](Tables.users.filter(_.id in usersId).result)
  }
}