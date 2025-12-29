```markdown
# Pipeline Big Data : Analyse de logs système en temps réel

## Description du projet
Ce projet est réalisé dans le cadre du module **Big Data / Data Engineering**.  
L’objectif est de concevoir un **pipeline Big Data complet** pour l’analyse de logs système en temps réel, en utilisant les technologies suivantes :

- **Apache Kafka** : ingestion et transport des logs
- **Apache Spark (RDD, DataFrame, Streaming, SQL)** : traitement distribué et analyse
- **HDFS** : stockage distribué des logs traités

Le pipeline simule la production de logs, leur traitement en streaming, leur stockage et leur analyse avec des requêtes SQL distribuées.

---

## Architecture du pipeline

```

Kafka Producer --> Kafka Topic --> Spark Streaming --> HDFS --> Spark SQL / Analyse

```

**Étapes principales :**
1. Les logs sont générés et envoyés dans un **topic Kafka** (`logs-topic`).  
2. **Spark Streaming** consomme les messages Kafka et les écrit dans **HDFS**.  
3. Les données stockées sont analysées avec **RDD, DataFrame et Spark SQL** pour obtenir des statistiques et identifier les erreurs critiques.

📌 *Remarque* : Les logs peuvent être simulés en temps réel via le terminal Kafka Producer.

---

## Technologies utilisées

- **Kafka** : Producer, Topic, Consumer
- **Spark** : RDD, DataFrame, Spark Streaming, Spark SQL
- **HDFS** : Stockage distribué
- **Scala** : Langage de programmation principal
- **Windows 10** avec Java 1.8+ pour l’environnement local

---

## Contenu du dépôt

Le dépôt contient les éléments suivants :

```

/LogStreamingApp
├─ build.sbt                  # Fichier de configuration SBT
├─ project/                   # Configuration du projet Scala
├─ src/main/scala/            # Code source Scala
│   └─ com/example/logstreaming/
│       └─ LogStreaming.scala # Code principal Spark Streaming
├─ README.md                  # Ce fichier explicatif
├─ rapport.pdf / rapport.docx  # Rapport complet du projet
└─ PPTX                        # Présentation du pipeline

````

---

## Instructions pour exécuter le projet

### Prérequis

- Java 1.8+  
- Apache Spark 3.x  
- Apache Kafka 2.x  
- Hadoop / HDFS 3.x  

### Étapes de lancement

1. **Démarrer Hadoop HDFS**  
```bat
cd C:\hadoop\sbin
start-dfs.cmd
````

2. **Démarrer Zookeeper**

```bat
cd C:\kafka
bin\windows\zookeeper-server-start.bat config\zookeeper.properties
```

3. **Démarrer Kafka Broker**

```bat
cd C:\kafka
bin\windows\kafka-server-start.bat config\server.properties
```

4. **Créer le topic Kafka**

```bat
bin\windows\kafka-topics.bat --create --topic logs-topic --bootstrap-server localhost:9092
```

5. **Envoyer des logs via Kafka Producer**

```bat
bin\windows\kafka-console-producer.bat --topic logs-topic --bootstrap-server localhost:9092
```

6. **Lancer Spark Streaming (code Scala)**

```bat
cd C:\Users\HP\Desktop\LogStreamingApp
sbt "runMain com.example.logstreaming.LogStreaming"
```

7. **Analyser les données dans Spark Shell (RDD, DataFrame, SQL)**

```scala
val logsRDD = sc.textFile("hdfs://localhost:9000/logs/output/*")
val logsDF = spark.read.text("hdfs://localhost:9000/logs/output/*")
logsDF.createOrReplaceTempView("logs")
spark.sql("SELECT level, COUNT(*) FROM logs GROUP BY level").show()
```

---

## Livrables du projet

1. **Code source complet** (Scala / Spark)
2. **Rapport** (PDF / DOCX) : explication du pipeline, architecture, résultats, captures d’écran
3. **PPTX** : présentation synthétique pour l’oral

---

## Remarques

* Les technologies utilisées peuvent être nouvelles pour l’étudiant, mais elles sont essentielles pour illustrer un pipeline Big Data complet.
* Les données générées sont simulées pour des fins pédagogiques.
* Le projet est conçu pour fonctionner en local mais reste extensible à un environnement distribué réel.

---

## Auteur

**Nom :** Sara El-otmani 6 Kenza El hariri
**Module :** Big Data / Data Engineering
**Encadrant :** Hassan BADIR
**Année :** 2025–2026
