package org.insightedge.geodemo.feeder.infinite.sender

import java.util.concurrent.BlockingQueue

import kafka.producer.{KeyedMessage, Producer}
import org.insightedge.geodemo.common.kafkaMessages.{OrderEvent, PickupEvent}
import play.api.libs.json.Json

/**
  * @author Vitaliy_Zinchenko
  */
class OrderSender(orders: BlockingQueue[OrderEvent],
                  producer: Producer[String, String],
                  waitingOrders: BlockingQueue[OrderEvent]) extends Thread {
  override def run(): Unit = {
    while (true) {
      val order = orders.take()
      send(order)
      waitingOrders.add(order)
    }
  }

  private def send(order: OrderEvent): Unit = {
    println(s"Sending order - $order")
//    implicit val locationWrites = Json.writes[OrderEvent]
//    producer.send(new KeyedMessage[String, String]("orders", Json.toJson(order).toString))
  }
}
