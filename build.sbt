lazy val root = (project in file(".")).
  settings(
    name := "galois",
    scalaVersion := "2.11.8"
  )

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

assemblyShadeRules in assembly := Seq(
  ShadeRule.rename("org.apache.kafka.**" -> "galois.kafka.@1").inLibrary(kafka)
)
