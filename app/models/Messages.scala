package models;

import slick.driver.PostgresDriver.api._

class Messages (tag: Tag) extends Table[Message](tag, "messages") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def message = column[String]("message")

  def * = (id.?, message) <> ((Message.apply _).tupled, Message.unapply _)
}

