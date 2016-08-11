package org.insightedge.geodemo.processing

import com.spatial4j.core.distance.DistanceUtils
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.insightedge.geodemo.common.gridModel.Request
import org.insightedge.geodemo.common.kafkaMessages.RequestEvent
import org.insightedge.spark.context.InsightEdgeConfig
import org.insightedge.spark.implicits.all._
import org.openspaces.spatial.ShapeFactory._
import play.api.libs.json.Json
import org.apache.log4j.{Level, Logger}

object DymanicPriceProcessor {

  implicit val requestReads = Json.reads[RequestEvent]

  def main(args: Array[String]): Unit = {
    val ieConfig = InsightEdgeConfig("insightedge-space", Some("insightedge"), Some("127.0.0.1"))
    val scConfig = new SparkConf().setAppName("GeospatialDemo").setMaster("local[2]").setInsightEdgeConfig(ieConfig)
    val ssc = new StreamingContext(scConfig, Seconds(1))
    val sc = ssc.sparkContext

    val rootLogger = Logger.getRootLogger
    rootLogger.setLevel(Level.ERROR)

    val requestsStream = initKafkaStream(ssc, "requests")

    import RddExtensionImplicit._

    requestsStream
      .map(message => Json.parse(message).as[RequestEvent])
      .map(event => Request(event.id, event.time, point(event.latitude, event.longitude), Seq()))
      .transform { rdd =>
        val query = "location spatial:within ?"
        val radius = 3 * DistanceUtils.KM_TO_DEG // TODO
        val queryParamsConstructor = (r: Request) => Seq(circle(r.location, radius))
        val updateRequest = (r: Request, nearRequests: Seq[Request]) =>  {
          r.copy(nearRequestsIds = nearRequests.map(_.id))
        }
        rdd.mapWithGridQuery(query, queryParamsConstructor, updateRequest)
      }
      .transform { rdd =>
        rdd.foreach(println)
        rdd
      }
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
