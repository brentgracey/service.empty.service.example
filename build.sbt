name := """empty-service-example"""

organization := "com.qordoba"
version := "2.0"
scalaVersion := "2.12.4"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)

libraryDependencies += guice

libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.0"

libraryDependencies += jdbc
libraryDependencies += ws


//data migration efforts only
resolvers ++= Seq(
  Resolver.typesafeRepo("releases"),
  Resolver.sonatypeRepo("releases")
)

dockerBaseImage := "anapsix/alpine-java:8"
