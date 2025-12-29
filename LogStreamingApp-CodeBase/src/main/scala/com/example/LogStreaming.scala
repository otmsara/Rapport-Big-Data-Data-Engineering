package com.example.logstreaming

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import org.apache.spark.sql.types._
import org.apache.hadoop.fs.{FileSystem, Path}
import java.net.URI

object LogStreaming {

  // Configuration de l'application
  object Config {
    val KAFKA_BOOTSTRAP_SERVERS = "localhost:9092"
    val KAFKA_TOPIC = "logs-topic"
    val HDFS_OUTPUT_PATH = "hdfs://localhost:9000/logs/output"
    val CHECKPOINT_PATH = "hdfs://localhost:9000/logs/checkpoint"
    val PROCESSING_INTERVAL = "10 seconds"
  }

  def main(args: Array[String]): Unit = {

    // 1. INITIALISATION SPARK
    println("🔧 Initialisation de la Spark Session...")

    val spark = SparkSession.builder()
      .appName("LogStreamingApp")
      .master("local[*]")
      .config("spark.sql.shuffle.partitions", "2")
      .config("spark.sql.streaming.checkpointLocation", Config.CHECKPOINT_PATH)
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")
    import spark.implicits._

    // 2. CRÉATION RÉPERTOIRES HDFS (API Hadoop)
    println("📁 Création des répertoires HDFS...")

    val fs = try {
      val hadoopConf = spark.sparkContext.hadoopConfiguration
      hadoopConf.set("fs.defaultFS", "hdfs://localhost:9000")
      hadoopConf.set("dfs.client.use.datanode.hostname", "true")

      FileSystem.get(new URI("hdfs://localhost:9000"), hadoopConf)
    } catch {
      case e: Exception =>
        println(s"⚠️ Impossible de se connecter à HDFS: ${e.getMessage}")
        println("⚠️ Démarrer HDFS avec: start-dfs.cmd")
        null
    }

    if (fs != null) {
      val outputPath = new Path("/logs/output")
      val checkpointPath = new Path("/logs/checkpoint")

      if (!fs.exists(outputPath)) {
        fs.mkdirs(outputPath)
        println(s"✅ Créé: /logs/output")
      }

      if (!fs.exists(checkpointPath)) {
        fs.mkdirs(checkpointPath)
        println(s"✅ Créé: /logs/checkpoint")
      }

      fs.close()
    } else {
      println("⚠️ Mode local activé (sans HDFS)")
    }

    // 3. LECTURE DEPUIS KAFKA
    println("📥 Connexion à Kafka...")

    val kafkaStream = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", Config.KAFKA_BOOTSTRAP_SERVERS)
      .option("subscribe", Config.KAFKA_TOPIC)
      .option("startingOffsets", "latest")
      .load()

    println(s"✅ Connecté à Kafka: ${Config.KAFKA_BOOTSTRAP_SERVERS}")
    println(s"📭 Topic: ${Config.KAFKA_TOPIC}")

    // 4. TRANSFORMATION SIMPLIFIÉE
    println("🔄 Transformation des logs...")

    val processedLogs = kafkaStream
      .select(
        col("timestamp").as("kafka_timestamp"),
        col("value").cast(StringType).as("raw_log")
      )
      .withColumn("level",
        when(col("raw_log").contains("ERROR"), "ERROR")
          .when(col("raw_log").contains("WARN"), "WARN")
          .when(col("raw_log").contains("INFO"), "INFO")
          .otherwise("OTHER")
      )
      .withColumn("processing_timestamp", current_timestamp())
      .withColumn("date", date_format(col("processing_timestamp"), "yyyy-MM-dd"))
      .withColumn("hour", hour(col("processing_timestamp")))

    // 5. AFFICHAGE CONSOLE PREMIER
    val consoleQuery = processedLogs
      .writeStream
      .outputMode(OutputMode.Append())
      .format("console")
      .option("truncate", "false")
      .trigger(Trigger.ProcessingTime("5 seconds"))
      .queryName("ConsoleOutput")
      .start()

    // 6. ÉCRITURE HDFS (optionnel - seulement si HDFS est disponible)
    if (fs != null) {
      println("💾 Configuration de l'écriture HDFS...")

      val hdfsQuery = processedLogs
        .writeStream
        .outputMode(OutputMode.Append())
        .format("parquet")
        .option("path", Config.HDFS_OUTPUT_PATH)
        .option("checkpointLocation", Config.CHECKPOINT_PATH)
        .partitionBy("date", "level")  // Partition par date puis niveau
        .trigger(Trigger.ProcessingTime(Config.PROCESSING_INTERVAL))
        .queryName("HDFSOutput")
        .start()
    }

    // 7. DÉMARRAGE
    println("🚀 Streaming démarré!")
    println("=" * 50)
    println("📤 Pour tester, envoyez des logs dans Kafka:")
    println("   kafka-console-producer --topic logs-topic --bootstrap-server localhost:9092")
    println("")
    println("📝 Exemples de logs à envoyer:")
    println("   INFO Application started")
    println("   WARN Memory usage high")
    println("   ERROR Database connection failed")
    println("=" * 50)
    println("🛑 CTRL+C pour arrêter")

    // 8. ATTENTE
    spark.streams.awaitAnyTermination()

    println("✅ Application arrêtée")
    spark.stop()
  }
}