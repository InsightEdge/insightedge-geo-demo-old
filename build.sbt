name := "insightedge-geo-demo"
version := "1.0"
scalaVersion := "2.10.4"

val playJson = "com.typesafe.play" %% "play-json" % "2.3.9"

val scalaTest = "org.scalatest" %% "scalatest" % "2.2.1" % "test"

val kafka = "org.apache.kafka" %% "kafka" % "0.8.2.2"

val commonDependencies = Seq(
  kafka
    exclude("javax.jms", "jms")
    exclude("com.sun.jdmk", "jmxtools")
    exclude("com.sun.jmx", "jmxri"),
  scalaTest,
  playJson
)

val feederDependencies = Seq(
  "io.spray" %%  "spray-json" % "1.3.2",
  "com.github.tototoshi" %% "scala-csv" % "1.3.3",
  "com.github.nscala-time" %% "nscala-time" % "1.8.0"
)

lazy val root = project.in(file(".")).aggregate(web, feeder)

lazy val web = project
  .enablePlugins(PlayScala)

lazy val feeder = project
  .settings(libraryDependencies ++= commonDependencies)
  .settings(libraryDependencies ++= feederDependencies)
