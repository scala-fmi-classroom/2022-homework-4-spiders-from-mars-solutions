name := "spiders-from-mars"
version := "0.1"

scalaVersion := "3.1.1"

scalacOptions ++= Seq(
  "-new-syntax",
  "-indent"
)

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.11",
  "org.asynchttpclient" % "async-http-client" % "2.12.3",
  "org.jsoup" % "jsoup" % "1.13.1",
  "org.scalatest" %% "scalatest" % "3.2.11" % Test
)

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}
