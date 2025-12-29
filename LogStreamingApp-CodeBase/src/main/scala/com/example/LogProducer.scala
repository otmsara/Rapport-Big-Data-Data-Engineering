// File: src/main/scala/com/example/LogProducer.scala
package com.example.logstreaming

import java.util.Properties
import org.apache.kafka.clients.producer._

object LogProducer {

  def main(args: Array[String]): Unit = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("acks", "1")

    val producer = new KafkaProducer[String, String](props)
    val topic = "logs-topic"

    val logLevels = Seq("INFO", "WARN", "ERROR", "DEBUG")
    val components = Seq("AuthService", "Database", "API", "Cache", "Security")

    var messageCount = 0

    println("🚀 Démarrage du Log Producer...")
    println("📤 Envoi de logs vers Kafka")
    println("🛑 CTRL+C pour arrêter")

    while (true) {
      val level = logLevels(scala.util.Random.nextInt(logLevels.length))
      val component = components(scala.util.Random.nextInt(components.length))
      val message = s"Simulated log message #${messageCount} at ${java.time.Instant.now}"
      val logMessage = s"$level $component: $message"

      val record = new ProducerRecord[String, String](topic, logMessage)

      producer.send(record, new Callback {
        override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
          if (exception != null) {
            println(s"❌ Erreur d'envoi: ${exception.getMessage}")
          } else {
            println(s"✅ Envoyé: $logMessage")
          }
        }
      })

      messageCount += 1
      Thread.sleep(1000) // 1 log par seconde
    }

    producer.close()
  }
}