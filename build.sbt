name := "akka-quickstart-scala"

version := "1.0"

scalaVersion := "2.12.6"

lazy val akkaVersion = "2.5.12"

EclipseKeys.withSource := true
EclipseKeys.withJavadoc := true

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.apache.httpcomponents" % "httpclient" % "4.5.5",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)
