package repository

import entity.{Group, Tables}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Rep

import scala.collection.immutable.Seq
import scala.concurrent.Future

class GroupRepository(db: Database = DatabaseStorage.db) {
  def saveReturningId(group: Group): DBIO[Int] = {
    Tables.groups returning Tables.groups.map(_.id) += group
  }

  def findUserGroups(groupsId: Query[Rep[Int], Int, Seq]): Future[Seq[Group]] = {
    db.run[Seq[Group]](Tables.groups.filter(_.id in groupsId).result)
  }
}