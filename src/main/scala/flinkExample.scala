import java.util
import java.util.Properties

import org.apache.flink.cep.PatternSelectFunction
import org.apache.flink.cep.scala.CEP
import org.apache.flink.streaming.api.scala._
import org.apache.flink.cep.scala.pattern.Pattern
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema

object flinkExample {
  def main(args: Array[String]): Unit = {

    val TEMPERATURE_THRESHOLD: Double = 50.00

    val see: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "localhost:9092")

    val src: DataStream[ObjectNode] = see.addSource(new FlinkKafkaConsumer010[ObjectNode]("broadcast", new JSONKeyValueDeserializationSchema(false), properties))

    var keyedStream = src.map(v => v.get("value"))
      .map {
        v =>
          val loc = v.get("locationID").asText()
          val temp = v.get("temp").asDouble()
          (loc, temp)
      }
      .keyBy(v => v._2)

    val pat = Pattern
      .begin[(String, Double)]("start")
      .where(_._2 > TEMPERATURE_THRESHOLD)

    val patternStream = CEP.pattern(keyedStream, pat)

    val result: DataStream[Map[String, Any]] = patternStream.select(
      new PatternSelectFunction[(String, Double), Map[String, Any]]() {
        override def select(pattern: util.Map[String, util.List[(String, Double)]]): Map[String, Any] = {
          val data = pattern.get("start").get(0) //alternative of iteration
          Map("locationID" -> data._1, "temperature" -> data._2)
        }
      }
    )

    result.print()
    see.execute("ASK Flink Kafka")
  }
}
