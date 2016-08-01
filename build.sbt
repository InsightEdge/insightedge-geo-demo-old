name := "insightedge-geo-demo"
version := "1.0"
scalaVersion := "2.11.8"

lazy val root = project.in(file(".")).aggregate(web, feeder)

lazy val web = project

lazy val feeder = project
