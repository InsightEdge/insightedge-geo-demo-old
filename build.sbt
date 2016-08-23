name := "insightedge-geo-demo"
version := "1.0"
scalaVersion := "2.10.4"

// if you want to run from IDE with embedded Spark set to 'true'
val runFromIde = false

val testLibs = Seq(
  "org.scalatest" %% "scalatest" % "2.2.1" % "test"
)

val kafkaLibs = Seq(
  "org.apache.kafka" %% "kafka" % "0.8.2.2"
    exclude("javax.jms", "jms")
    exclude("com.sun.jdmk", "jmxtools")
    exclude("com.sun.jmx", "jmxri")
)

val jsonLibs = Seq(
  "com.typesafe.play" %% "play-json" % "2.3.9"
)

def insightEdgeLibs(scope: String) = Seq(
  "org.gigaspaces.insightedge" % "insightedge-core" % "1.0.0" % scope exclude("javax.jms", "jms"),
  "org.gigaspaces.insightedge" % "insightedge-scala" % "1.0.0" % scope exclude("javax.jms", "jms")
)
val openspaceResolvers = Seq(
  Resolver.mavenLocal,
  "Openspaces Maven Repository" at "http://maven-repository.openspaces.org"
)

lazy val root = project.in(file(".")).aggregate(web, feeder, insightedgeProcessing)

lazy val commonGridModel = project.in(file("common-grid-model"))
  .settings(resolvers ++= openspaceResolvers)
  .settings(libraryDependencies ++= insightEdgeLibs("provided"))

lazy val commonKafkaModel = project.in(file("common-kafka-model"))
  .settings(libraryDependencies ++= kafkaLibs)

lazy val web = project
  .enablePlugins(PlayScala)
  .settings(libraryDependencies ++= insightEdgeLibs("compile"))
  .dependsOn(commonGridModel, commonKafkaModel)

lazy val feeder = project
  .settings(libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.3.3")
  .settings(libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "1.8.0")
  .settings(libraryDependencies ++= testLibs)
  .settings(libraryDependencies ++= jsonLibs)
  .dependsOn(commonKafkaModel)

lazy val insightedgeProcessing = project.in(file("insightedge-processing"))
  .settings(libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka" % "1.6.0")
  .settings(libraryDependencies ++= testLibs)
  .settings(libraryDependencies ++= jsonLibs)
  .settings(libraryDependencies ++= insightEdgeLibs(if (runFromIde) "compile" else "provided"))
  .settings(
    assemblyMergeStrategy in assembly := {
      case PathList("org", "apache", "spark", "unused", "UnusedStubClass.class") => MergeStrategy.first
      case x => (assemblyMergeStrategy in assembly).value(x)
    },
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
  )
  .dependsOn(commonGridModel, commonKafkaModel)
