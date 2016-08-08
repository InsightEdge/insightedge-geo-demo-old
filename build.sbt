name := "insightedge-geo-demo"
version := "1.0"
scalaVersion := "2.10.4"

resolvers += Resolver.mavenLocal
resolvers += "Openspaces Maven Repository" at "http://maven-repository.openspaces.org"


val commonDependencies = Seq(
  "org.apache.kafka" %% "kafka" % "0.8.2.2"
    exclude("javax.jms", "jms")
    exclude("com.sun.jdmk", "jmxtools")
    exclude("com.sun.jmx", "jmxri"),
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "com.typesafe.play" %% "play-json" % "2.3.9"
)

val feederDependencies = Seq(
  "io.spray" %%  "spray-json" % "1.3.2",
  "com.github.tototoshi" %% "scala-csv" % "1.3.3",
  "com.github.nscala-time" %% "nscala-time" % "1.8.0"
)

val insightedgeDependencies = Seq(
  "org.gigaspaces.insightedge" % "insightedge-core" % "1.0.0" % "compile" exclude("javax.jms", "jms"),
  "org.gigaspaces.insightedge" % "insightedge-scala" % "1.0.0" % "compile" exclude("javax.jms", "jms"),
  "org.apache.spark" %% "spark-streaming-kafka" % "1.6.0"
)


lazy val root = project.in(file(".")).aggregate(web, feeder, insightedge)

lazy val web = project
  .enablePlugins(PlayScala)

lazy val feeder = project
  .settings(libraryDependencies ++= commonDependencies)
  .settings(libraryDependencies ++= feederDependencies)

lazy val insightedge = project.in(file("insightedge-processing"))
  .settings(libraryDependencies ++= commonDependencies)
  .settings(libraryDependencies ++= insightedgeDependencies)
