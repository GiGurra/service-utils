val service_utils = Project(id = "service-utils", base = file("."))
  .settings(
    organization := "com.github.gigurra",
    version := "0.1.13-SNAPSHOT",

    scalaVersion := "2.11.8",
    scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation"),

    libraryDependencies ++= Seq(
      "com.twitter"         %%  "finagle-http"                %   "6.38.0",
      "org.json4s"          %%  "json4s-core"                 %   "3.4.0",
      "org.json4s"          %%  "json4s-jackson"              %   "3.4.0",
      "com.github.gigurra"  %%  "franklin-heisenberg-bridge"  %   "0.1.20",
      "org.scalatest"       %%  "scalatest"                   %   "2.2.4"           %   "test",
      "org.mockito"         %   "mockito-core"                %   "1.10.19"         %   "test"
    ),

    resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"
  )
