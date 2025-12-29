name := "LogStreamingApp"
version := "1.0.0"
scalaVersion := "2.12.15"

val sparkVersion = "3.4.1"
val kafkaVersion = "3.4.0"

// Stratégie de fusion pour sbt-assembly
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

// Nom du JAR de sortie
assembly / assemblyJarName := s"${name.value}-${version.value}-assembly.jar"

// Exclure Scala du JAR (car Spark le fournit déjà)
assembly / assemblyOption := (assembly / assemblyOption).value
  .withIncludeScala(false)

libraryDependencies ++= Seq(
  // Spark - SANS "provided" pour l'assembly
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion,

  // Kafka
  "org.apache.kafka" % "kafka-clients" % kafkaVersion,

  // Logging
  "org.apache.logging.log4j" % "log4j-api" % "2.17.2",
  "org.apache.logging.log4j" % "log4j-core" % "2.17.2",

  // Configuration
  "com.typesafe" % "config" % "1.4.2"
)

// Options de compilation
scalacOptions ++= Seq(
  "-encoding", "UTF-8",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlint"
)

// Pour éviter les erreurs de dépassement de méthode
Test / fork := true
javaOptions ++= Seq("-Xms512M", "-Xmx2048M", "-XX:MaxPermSize=2048M", "-XX:+CMSClassUnloadingEnabled")

// Configuration pour exécuter depuis IntelliJ
Compile / run := Defaults.runTask(
  Compile / fullClasspath,
  Compile / run / mainClass,
  Compile / run / runner
).evaluated