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


  def index = Action.async {
    val msg: Future[Seq[Message]] = Messages.list
    msg.map(m => Ok(Json.toJson(m)))
  }

  def create = Action.async(BodyParsers.parse.json) { request => 
    val message = request.body.validate[Message]
    message.fold(
      errors => 
        Future(BadRequest(Json.obj("status" -> "Parsing message failed", "message" -> JsError.toJson(errors)))),
      message => 
        Messages.create(message).map(m =>
            Ok(Json.obj("status" -> "Success", "message" -> Json.toJson(m))))
    )
    
  }

  def findOne(id: Int) = Action.async {
    val message: Future[Message] = Messages.show(id)
    message.map(msg => Ok(Json.toJson(msg)))
  }

  def update(id: Int) = Action.async(BodyParsers.parse.json) { request =>
    val message = request.body.validate[Message]
    message.fold(
      errors => 
        Future(BadRequest(Json.obj("status" -> "Message update failed", "message" -> JsError.toJson(errors)))),
      message => {
        Messages.update(id, message)
        Future(Ok(Json.obj("message" -> "OK")))
      }
    )
  }

  def delete(id: Int) = Action.async {
    Messages.delete(id).map(m => Ok("deleted"))
  }
}
