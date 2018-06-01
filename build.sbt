name := "akka-quickstart-scala"

version := "1.0"

scalaVersion := "2.12.6"

lazy val akkaVersion = "2.5.12"
lazy val akkaHttpVersion = "10.1.1"
lazy val akkaStreamVersion = "2.5.11"

EclipseKeys.withSource := true
EclipseKeys.withJavadoc := true

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaStreamVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "org.apache.httpcomponents" % "httpclient" % "4.5.5",

  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)
