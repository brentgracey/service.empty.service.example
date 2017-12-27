import com.typesafe.sbt.packager.docker.Cmd

name := """empty-service-example"""

organization := "com.qordoba"
version := "2.0"
scalaVersion := "2.11.11"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)

lazy val appOperationalDatastoreService = project
lazy val appNormalizationService = project

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test

libraryDependencies += "com.google.cloud.bigtable" % "bigtable-hbase-1.x" % "1.0.0-pre3"

libraryDependencies += "io.netty" % "netty-tcnative-boringssl-static" % "1.1.33.Fork26"

libraryDependencies += "commons-codec" % "commons-codec" % "1.10"

libraryDependencies += "com.google.cloud" % "google-cloud-pubsub" % "0.30.0-beta"

libraryDependencies += jdbc
libraryDependencies += ws


//data migration efforts only
resolvers ++= Seq(
  Resolver.typesafeRepo("releases"),
  Resolver.sonatypeRepo("releases")
)
libraryDependencies ++= Seq(
  "com.outworkers"  %% "phantom-dsl"  % "2.14.5"
)

/** http://www.scala-sbt.org/sbt-native-packager/ **/

dockerBaseImage := "frolvlad/alpine-oraclejdk8"

dockerCommands := dockerCommands.value.flatMap{
  case cmd@Cmd("FROM",_) => List(cmd, Cmd("RUN", "apk update && apk add bash"))
  case other => List(other)
}