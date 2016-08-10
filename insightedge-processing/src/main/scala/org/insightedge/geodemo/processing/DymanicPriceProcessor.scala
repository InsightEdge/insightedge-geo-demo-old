package org.insightedge.geodemo.processing

import java.util.Collections

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.insightedge.geodemo.common.dto.RequestEvent
import org.insightedge.geodemo.common.model.Request
import org.insightedge.spark.context.InsightEdgeConfig
import org.insightedge.spark.implicits.all._
import play.api.libs.json.Json
import org.apache.log4j.{Level, Logger}

object DymanicPriceProcessor {

  implicit val requestReads = Json.reads[RequestEvent]

  def main(args: Array[String]): Unit = {
    val ieConfig = InsightEdgeConfig("insightedge-space", Some("insightedge"), Some("127.0.0.1"))
    val scConfig = new SparkConf().setAppName("GeospatialDemo").setMaster("local[2]").setInsightEdgeConfig(ieConfig)
    val ssc = new StreamingContext(scConfig, Seconds(1))

    val rootLogger = Logger.getRootLogger
    rootLogger.setLevel(Level.ERROR)

    val requestsStream = initKafkaStream(ssc, "requests")

    requestsStream
      .map(m => Json.parse(m).as[RequestEvent])
      .transform(rdd => { rdd.foreach(println(_)); rdd })
      .map(e => Request(e.id, e.time, e.latitude, e.longitude, Collections.emptyList()))
      .saveToGrid()

    ssc.start()
    ssc.awaitTermination()
  }

  /**
    * Creates kafka stream with brokers and topic
    */
  private def initKafkaStream(ssc: StreamingContext, topic: String): DStream[String] = {
    KafkaUtils.createStream(ssc, "127.0.0.1:2181", "geo-demo", Map(topic -> 1)).map(_._2)
  }

}
