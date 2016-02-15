val service_utils = Project(id = "service-utils", base = file("."))
  .settings(
    organization := "se.gigurra",
    version := scala.util.Properties.envOrElse("SERVICE_UTILS_VERSION", "SNAPSHOT"),

    scalaVersion := "2.11.7",
    scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation"),

    libraryDependencies ++= Seq(
      "com.twitter"       %%  "finagle-http"    %   "6.31.0",
      "org.json4s"        %%  "json4s-core"     %   "3.3.0",
      "org.json4s"        %%  "json4s-jackson"  %   "3.3.0",
      "org.scalatest"     %%  "scalatest"       %   "2.2.4"     %   "test",
      "org.mockito"       %   "mockito-core"    %   "1.10.19"   %   "test"
    ),

    resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"

  )
  .dependsOn(uri("git://github.com/GiGurra/franklin-heisenberg-bridge.git#0.1.15"))
