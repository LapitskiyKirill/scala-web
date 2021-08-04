package repository

import slick.jdbc.JdbcBackend
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._

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