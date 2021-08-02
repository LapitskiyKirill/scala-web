name := "scala-web"

version := "0.1"

scalaVersion := "2.13.6"

val AkkaVersion = "2.6.15"
val AkkaHttpVersion = "10.2.5"
libraryDependencies += "com.typesafe" % "config" % "1.4.1"
libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.13.6"
libraryDependencies += "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.3"
libraryDependencies += "com.typesafe.slick" %% "slick" % "3.3.3"
libraryDependencies += "org.postgresql" % "postgresql" % "42.2.23"
libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.7.32"
libraryDependencies += "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion
)
enablePlugins(FlywayPlugin)
version := "0.0.1"
name := "flyway-sbt"

libraryDependencies += "org.postgresql" % "postgresql" % "42.2.23"

flywayUrl := "jdbc:postgresql://localhost:5432/scala_users"
flywayUser := "postgres"
flywayPassword := "12345678"
flywayLocations += "src/main/resources/db/migration"
flywaySchemas += "public"
