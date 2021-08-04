import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import controller.{GroupController, GroupUserController, UserController}
import util.Config

import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.StdIn

object Main {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem(Behaviors.empty, Config.actorSystemName)

    val route = concat(
      (new GroupController).getRoute,
      (new UserController).getRoute,
      (new GroupUserController).getRoute
    )

    val bindingFuture = Http().newServerAt(Config.path, Config.port).bind(route)

    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}