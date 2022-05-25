
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
scalacOptions += "-Ykind-projector:underscores"
