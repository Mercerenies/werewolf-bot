
lazy val root = (project in file("."))
  .settings(
    inThisBuild(List(
      organization := "com.mercerenies.werewolf",
      scalaVersion := "3.0.0"
    )),
    name := "werewolf"
  )

Compile / unmanagedSourceDirectories += baseDirectory.value / "src"
libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.4.0-M7"
libraryDependencies += "org.javacord" % "javacord" % "3.4.0"
libraryDependencies +=  "org.apache.logging.log4j" % "log4j-core" % "2.17.2" % Runtime
scalacOptions += "-Ykind-projector:underscores"
