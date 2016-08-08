package org.insightedge.geodemo.processing

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.insightedge.spark.context.InsightEdgeConfig
import org.insightedge.spark.implicits.all._

object DymanicPriceProcessor {

  def main(args: Array[String]): Unit = {
    val ieConfig = InsightEdgeConfig("insightedge-space", Some("insightedge"), Some("127.0.0.1"))
    val scConfig = new SparkConf().setAppName("GeospatialDemo").setMaster("local[2]").setInsightEdgeConfig(ieConfig)
    val ssc = new StreamingContext(scConfig, Seconds(1))

    val requestsStream = initKafkaStream(ssc, "requests")

    requestsStream.foreachRDD { rdd =>
      rdd.foreach(println(_))
    }
    ssc.start()
    ssc.awaitTermination()
  }

  /*
   Creates direct kafka stream with brokers and topic
    */
  private def initKafkaStream(ssc: StreamingContext, topic: String): DStream[String] = {
    KafkaUtils.createStream(ssc, "127.0.0.1:2181", "geo-demo", Map(topic -> 1)).map(_._2)
  }

}
