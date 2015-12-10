enablePlugins(JavaAppPackaging)

name := "api-relay"
version := "1.0"
scalaVersion := "2.11.7"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV       = "2.4.0"
  val akkaStreamV = "2.0-M2"
  val scalaTestV  = "2.2.5"
  val couchbase   = "2.1.2"
  val json4s      = "3.3.0"
  Seq(
    "com.typesafe.akka"     %% "akka-actor"                           % akkaV,
    "com.typesafe.akka"     %% "akka-stream-experimental"             % akkaStreamV,
    "com.typesafe.akka"     %% "akka-http-core-experimental"          % akkaStreamV,
    "com.typesafe.akka"     %% "akka-http-experimental"               % akkaStreamV,
    "com.typesafe.akka"     %% "akka-http-spray-json-experimental"    % akkaStreamV,
    "com.typesafe.akka"     %% "akka-http-testkit-experimental"       % akkaStreamV,
    "org.scalatest"         %% "scalatest"                            % scalaTestV % "test",
    "com.couchbase.client"   % "java-client"                          % couchbase,
    "org.json4s"            %% "json4s-jackson"                       % json4s
  )
}

Revolver.settings
