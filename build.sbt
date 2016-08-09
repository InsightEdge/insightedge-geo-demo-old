name := "insightedge-geo-demo"
version := "1.0"
scalaVersion := "2.10.4"

val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % "2.2.1" % "test"
)

val commonDependencies = Seq(
  "org.apache.kafka" %% "kafka" % "0.8.2.2"
    exclude("javax.jms", "jms")
    exclude("com.sun.jdmk", "jmxtools")
    exclude("com.sun.jmx", "jmxri"),
  "com.typesafe.play" %% "play-json" % "2.3.9",
  "org.gigaspaces.insightedge" % "insightedge-core" % "1.0.0" % "compile" exclude("javax.jms", "jms"),
  "org.gigaspaces.insightedge" % "insightedge-scala" % "1.0.0" % "compile" exclude("javax.jms", "jms")
)
val commonResolvers = Seq(
  Resolver.mavenLocal,
  "Openspaces Maven Repository" at "http://maven-repository.openspaces.org"
)

val feederDependencies = Seq(
  "io.spray" %% "spray-json" % "1.3.2",
  "com.github.tototoshi" %% "scala-csv" % "1.3.3",
  "com.github.nscala-time" %% "nscala-time" % "1.8.0"
)

val insightedgeDependencies = Seq(
  "org.apache.spark" %% "spark-streaming-kafka" % "1.6.0"
)


lazy val root = project.in(file(".")).aggregate(web, feeder, insightedge)

lazy val common = project.in(file("common"))
  .settings(resolvers ++= commonResolvers)
  .settings(libraryDependencies ++= commonDependencies)
  .settings(libraryDependencies ++= testDependencies)

lazy val web = project
  .settings(libraryDependencies ++= testDependencies)
  .enablePlugins(PlayScala)
  .dependsOn(common)

lazy val feeder = project
  .settings(libraryDependencies ++= feederDependencies)
  .settings(libraryDependencies ++= testDependencies)
  .dependsOn(common)

lazy val insightedge = project.in(file("insightedge-processing"))
  .settings(libraryDependencies ++= insightedgeDependencies)
  .settings(libraryDependencies ++= testDependencies)
  .dependsOn(common)
