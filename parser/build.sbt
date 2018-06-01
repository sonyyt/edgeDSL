import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.1",
      version := "0.1.0-SNAPSHOT"
    )),

    name := "Hello",
    libraryDependencies += scalaTest % Test,
    libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.0-M1",
    libraryDependencies += "org.scala-lang.modules" % "scala-parser-combinators_2.12" % "1.0.6",
    // https://mvnrepository.com/artifact/org.scalaj/scalaj-http_2.12
    libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.3.0"
    //libraryDependencies += "org.json4s" %% "json4s-native" % "3.3.0",
    //libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.3.0"
  )