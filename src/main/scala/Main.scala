import _root_.entity.{UserGroup, _}
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives.{post, _}
import controller.{GroupController, UserController, UserGroupController}
import repository.{BaseRepository, GroupRepository, UserGroupRepository, UserRepository}
import spray.json.DefaultJsonProtocol._
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.StdIn

object Main {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem(Behaviors.empty, "my-system")

    val route = concat(
      (new GroupController).getRoute,
      (new UserController).getRoute,
      (new UserGroupController).getRoute
    )

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)

    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}