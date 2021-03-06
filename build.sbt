import sbt.Keys._
import sbtassembly.AssemblyPlugin.autoImport._

val publishClientAssembly = sys.env.getOrElse("PUBLISH_CLIENT_ASSEMBLY", "false").toBoolean

lazy val root = (project in file(".")).
  settings(
    organization := "in.ashwanthkumar",
    name := "galois",
    version := "0.4-SNAPSHOT",
    scalaVersion := "2.11.8"
  ).
  settings(publishSettings: _*).
  settings(clientAssemblySettings: _*)

val kafka = "org.apache.kafka" % "kafka-clients" % "0.10.0.0"
libraryDependencies += kafka

libraryDependencies += "com.twitter" %% "algebird-core" % "0.12.1"

libraryDependencies += "org.rocksdb" % "rocksdbjni" % "4.1.0"

libraryDependencies += "com.twitter" %% "chill-bijection" % "0.8.0"

libraryDependencies += "com.google.inject" % "guice" % "4.1.0"

libraryDependencies += "com.typesafe.akka" %% "akka-http-core" % "2.4.7"

libraryDependencies += "com.typesafe.akka" %% "akka-http-experimental" % "2.4.7"

libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.7"

libraryDependencies += "com.fasterxml.jackson.module" % "jackson-module-scala_2.10" % "2.8.3"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % Test

libraryDependencies += "org.mockito" % "mockito-all" % "1.10.19" % Test

val _pomExtra =
  <url>http://github.com/vinothkr/galois</url>
    <licenses>
      <license>
        <name>Apache License, Version 2.0</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:vinothkr/galois.git</url>
      <connection>scm:git:git@github.com:vinothkr/galois.git</connection>
    </scm>
    <developers>
      <developer>
        <id>vinothkr</id>
        <name>Vinothkumar</name>
      </developer>
    </developers>

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  crossPaths := true,
  publishArtifact in Test := false,
  publishArtifact in(Compile, packageDoc) := true,
  publishArtifact in(Compile, packageSrc) := true,
  publishTo <<= version { (v: String) =>
    val nexus = "https://oss.sonatype.org/"
    if (v.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  pomExtra := _pomExtra,
  addArtifact(artifact in(Compile, assembly), assembly)
)

lazy val clientAssemblySettings = if (publishClientAssembly) {
  Seq(
    // hackery to include only specific jars
    // since assembly doesn't provide a way to specifically include jars - we have to resort to this
    assemblyExcludedJars in assembly := {
      val includedJars = List("kafka-clients")
      val cp = (fullClasspath in assembly).value
      cp filterNot { path =>
        includedJars.exists(path.data.getName.startsWith)
      }
    },
    assemblyShadeRules in assembly := Seq(
      ShadeRule.rename("org.apache.kafka.**" -> "galois.kafka.@1").inLibrary(kafka).inProject.inAll
    ),
    assemblyOption in assembly := (assemblyOption in assembly).value,
    artifact in(Compile, assembly) := {
      val art = (artifact in(Compile, assembly)).value
      art.copy(`classifier` = Some("assembly"))
    }
  )
} else Seq()
