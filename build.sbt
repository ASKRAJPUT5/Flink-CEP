name := "CEPdone"

version := "0.1"

scalaVersion := "2.11.8"

libraryDependencies += "org.apache.flink" %% "flink-streaming-scala" % "1.9.0"

libraryDependencies += "org.apache.kafka" %% "kafka" % "2.3.0"

libraryDependencies += "org.apache.flink" %% "flink-connector-kafka-0.10" % "1.9.0"

libraryDependencies += "org.apache.flink" %% "flink-cep-scala" % "1.9.0"
