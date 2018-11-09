name := "orders-matcher"

version := "0.1"

scalaVersion := "2.12.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.17",
  "org.scalatest" % "scalatest_2.12" % "3.0.5" % "test",
  "com.typesafe.akka" %% "akka-slf4j" % "2.5.17",
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)