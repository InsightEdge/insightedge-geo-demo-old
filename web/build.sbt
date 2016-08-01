name := "web"
version := "1.0"
scalaVersion := "2.11.8"

lazy val web = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq("org.specs2" %% "specs2-core" % "3.8.4" % "test")
