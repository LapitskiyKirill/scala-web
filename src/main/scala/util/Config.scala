package util

import com.typesafe.config.{Config, ConfigFactory}

object Config {
  val config: Config = ConfigFactory.load
  val path: String = config.getString("server.path")
  val port: Int = config.getInt("server.port")
  val actorSystemName: String = config.getString("actorSystemName")
}