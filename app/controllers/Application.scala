package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.api.Play.current
import play.api.libs.json._
import play.api.mvc._

import models.Message
import models.Messages


class Application extends Controller {


  def index = Action.async {
    val msg: Future[Seq[Message]] = Messages.list
    msg.map(m => Ok(Json.obj("status" -> "Ok", "messages" -> Json.toJson(m))))
  }

  def create = Action.async(BodyParsers.parse.json) { request => 
    val message = request.body.validate[Message]
    message.fold(
      errors => 
        Future(BadRequest(Json.obj(
          "status" -> "Parsing message failed",
          "error" -> JsError.toJson(errors)
        ))),
      message => 
        Messages.create(message).map(m =>
            Ok(Json.obj(
              "status" -> "Success",
              "message" -> Json.toJson(m)
            ))
        )
    )
  }

  def show(id: Int) = Action.async {
    val message: Future[Message] = Messages.show(id)
    message.map(msg => Ok(Json.obj("status" -> "Ok", "message" -> Json.toJson(msg))))
  }

  def update(id: Int) = Action.async(BodyParsers.parse.json) { request =>
    val message = request.body.validate[Message]
    message.fold(
      errors => 
        Future(BadRequest(Json.obj(
          "status" -> "Message update failed",
          "error" -> JsError.toJson(errors)
        ))),
      message => {
        Messages.update(id, message)
        Future(Ok(Json.obj("status" -> "Ok", "message" -> Json.toJson(message))))
      }
    )
  }

  def delete(id: Int) = Action.async {
    Messages.delete(id).map(m => Ok(Json.obj("status" -> "Ok")))
  }
}
