name := """my-perfect-weather"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

//Docker stuff
dockerBaseImage := "radek1st/java8-scala-sbt"

maintainer := "Radek Ostrowski dest.hawaii@gmail.com"

dockerExposedPorts in Docker := Seq(80)

//Need root to run on port 80
daemonUser in Docker := "root"
