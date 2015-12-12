package models;

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import slick.driver.JdbcProfile
import slick.driver.PostgresDriver.api._
import play.api.db.slick.DatabaseConfigProvider
import play.api._
import play.api.mvc._

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Message (
  id: Option[Int],
  message: String
)

object Message {
  implicit val messageReads: Reads[Message] = (
    (JsPath \ "id").readNullable[Int] and
    (JsPath \ "message").read[String]
  )(Message.apply _)


  implicit val messageWrites: Writes[Message] = (
    (JsPath \ "id").writeNullable[Int] and
    (JsPath \ "message").write[String]
  )(unlift(Message.unapply _))
}

class Messages (tag: Tag) extends Table[Message](tag, "messages") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def message = column[String]("message")

  def * = (id.?, message) <> ((Message.apply _).tupled, Message.unapply _)
}

object Messages {

  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  val messages = TableQuery[Messages]

  def findQuery(id: Int) = {
    messages.filter(_.id === id)
  }

  def list = {
    dbConfig.db.run(messages.result)
  }

  def create(message: Message) = {
    dbConfig.db.run(messages returning messages += message)
  }

  def show(id: Int): Future[Message] = {
    dbConfig.db.run(findQuery(id).result.head)
  }

  def update(id: Int, message: Message) = {
    dbConfig.db.run(findQuery(id).update(message))
  }

  def delete(id: Int) = {
    dbConfig.db.run(findQuery(id).delete)
  }

}
