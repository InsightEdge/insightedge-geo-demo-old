package org.insightedge.geodemo.processing

import com.spatial4j.core.distance.DistanceUtils
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.insightedge.geodemo.common.gridModel.{NewOrder, OrderRequest, ProcessedOrder}
import org.insightedge.geodemo.common.kafkaMessages.{OrderEvent, PickupEvent}
import org.insightedge.spark.context.InsightEdgeConfig
import org.insightedge.spark.implicits.all._
import org.openspaces.spatial.ShapeFactory._
import play.api.libs.json.Json
import org.apache.log4j.{Level, Logger}

object DymanicPriceProcessor {

  implicit val orderEventReads = Json.reads[OrderEvent]
  implicit val pickupEventReads = Json.reads[PickupEvent]

  def main(args: Array[String]): Unit = {
    val ieConfig = InsightEdgeConfig("insightedge-space", Some("insightedge"), Some("127.0.0.1"))
    val scConfig = new SparkConf().setAppName("GeospatialDemo").setMaster("local[*]").setInsightEdgeConfig(ieConfig)
    val ssc = new StreamingContext(scConfig, Seconds(1))
    ssc.checkpoint("checkpoint")
    val sc = ssc.sparkContext

    val rootLogger = Logger.getRootLogger
    rootLogger.setLevel(Level.ERROR)

    val ordersStream = initKafkaStream(ssc, "orders")
    val pickupsStream = initKafkaStream(ssc, "pickups")

    ordersStream
      .map(message => Json.parse(message).as[OrderEvent])
      .transform { rdd =>
        val query = "location spatial:within ? AND status = ?"
        val radius = 3 * DistanceUtils.KM_TO_DEG
        val queryParamsConstructor = (e: OrderEvent) => Seq(circle(point(e.longitude, e.latitude), radius), NewOrder)
        rdd.zipWithGridSql[OrderRequest](query, queryParamsConstructor, None)
      }
      .map { case (e: OrderEvent, nearOrders: Seq[OrderRequest]) =>
        val location = point(e.longitude, e.latitude)
        val nearOrderIds = nearOrders.map(_.id)
        val priceFactor = if (nearOrderIds.length > 3) {
          1.0 + (nearOrderIds.length - 3) * 0.1
        } else {
          1.0
        }
        OrderRequest(e.id, e.time, location, priceFactor, nearOrderIds, NewOrder)
      }
      .transform { rdd =>
        rdd.foreach(println)
        rdd
      }
      .saveToGrid()


    pickupsStream
      .transform { rdd =>
        rdd.foreach(println)
        rdd
      }
      .map(message => Json.parse(message).as[PickupEvent])
      .transform { rdd =>
        val query = "id = ?"
        val queryParamsConstructor = (e: PickupEvent) => Seq(e.orderId)
        rdd.zipWithGridSql[OrderRequest](query, queryParamsConstructor, None)
      }
      .flatMap { case (e: PickupEvent, orders: Seq[OrderRequest]) =>
        // there should be only 1 order unless we receive incorrect data
        orders.map(_.copy(status = ProcessedOrder))
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
