package controllers

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import slick.driver.JdbcProfile
import slick.driver.PostgresDriver.api._
import play.api.db.slick.DatabaseConfigProvider

import play.api.libs.json._
import play.api.mvc._

import models.Message
import models.Messages

class Application @Inject()(dbConfigProvider: DatabaseConfigProvider) extends Controller {

  val dbConfig = dbConfigProvider.get[JdbcProfile]

  val messages = TableQuery[Messages]
  def findQuery(id: Int) = messages.filter(_.id === id)

  def index = Action.async {
    val message = dbConfig.db.run(messages.result)
    message.map(msg => Ok(Json.obj("status" -> "Ok", "messages" -> Json.toJson(msg))))
  }

  def create = Action.async(BodyParsers.parse.json) { request => 
    val message = request.body.validate[Message]
    message.fold(
      errors => Future(BadRequest(Json.obj(
          "status" -> "Parsing message failed",
          "error" -> JsError.toJson(errors)))),
      message => 
        dbConfig.db.run(messages returning messages += message).map(m =>
            Ok(Json.obj( "status" -> "Success", "message" -> Json.toJson(m)))
        )
    )
  }

  def show(id: Int) = Action.async {
    val message = dbConfig.db.run(findQuery(id).result.head)
    message.map(msg => Ok(Json.obj("status" -> "Ok", "message" -> Json.toJson(msg))))
  }

  def update(id: Int) = Action.async(BodyParsers.parse.json) { request =>
    val message = request.body.validate[Message]
    message.fold(
      errors => Future(BadRequest(Json.obj(
          "status" -> "Message update failed",
          "error" -> JsError.toJson(errors)))),
      message => {
        dbConfig.db.run(findQuery(id).update(message))
        Future(Ok(Json.obj("status" -> "Ok", "message" -> Json.toJson(message))))
      }
    )
  }

  def delete(id: Int) = Action.async {
    dbConfig.db.run(findQuery(id).delete).map(m => Ok(Json.obj("status" -> "Ok")))
  }
}
