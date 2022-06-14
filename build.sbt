
lazy val root = (project in file("."))
  .settings(
    inThisBuild(List(
      organization := "com.mercerenies.werewolf",
      scalaVersion := "3.1.0"
    )),
    name := "werewolf"
  )

Compile / run / fork := true
// Compile / javaOptions += "-Dlog4j.debug"

Compile / unmanagedSourceDirectories += baseDirectory.value / "src" / "main"
Compile / unmanagedResourceDirectories += baseDirectory.value / "res"

Test / unmanagedSourceDirectories += baseDirectory.value / "src" / "test"
Test / unmanagedResourceDirectories += baseDirectory.value / "res"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.4.0-M7"
libraryDependencies += "org.javacord" % "javacord" % "3.4.0"
libraryDependencies +=  "org.apache.logging.log4j" % "log4j-core" % "2.17.2" % Runtime
libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.12"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.12" % "test"
libraryDependencies += "org.scalatestplus" %% "mockito-3-12" % "3.2.10.0" % "test"
scalacOptions += "-Ykind-projector:underscores"
