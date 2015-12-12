package controllers

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.api.Play.current
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import slick.driver.PostgresDriver.api._
import play.api._
import play.api.mvc._
import models.Message
import models.Messages

import play.api.libs.json._
import play.api.libs.functional.syntax._


class Application extends Controller {

  implicit val messageWrites: Writes[Message] = (
    (JsPath \ "id").write[Int] and
    (JsPath \ "message").write[String]
  )(unlift(Message.unapply _))

  implicit val messageReads: Reads[Message] = (
    (JsPath \ "id").read[Int] and
    (JsPath \ "message").read[String]
  )(Message.apply _)

  def index = Action.async {
    val msg: Future[Seq[Message]] = Messages.list
    msg.map(m => {
      val json = Json.toJson(m)
      Ok(json)
    })
  }

  def findOne(id: Int) = Action.async {
    val message: Future[Message] = Messages.show(id)
    message.map(msg => Ok(Json.toJson(msg)))
  }

  def update(id: Int) = Action.async(BodyParsers.parse.json) { request =>
    val message = request.body.validate[Message]
    message.fold(
      errors => {
        Future(BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toJson(errors))))
      },
      message => {
        Messages.update(id, message)
        Future(Ok(Json.toJson(Map("message" -> "OK"))))
      }
    )
  }

  def delete(id: Int) = Action.async {
    Messages.delete(id).map(m => Ok("deleted"))
  }
}
