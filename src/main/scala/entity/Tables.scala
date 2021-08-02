package entity

import slick.lifted.TableQuery

object Tables {
  val users = TableQuery[UserTable]
  val groups = TableQuery[GroupTable]
  val userGroup = TableQuery[UserGroupTable]
}
