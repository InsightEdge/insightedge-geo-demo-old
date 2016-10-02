package org.insightedge.geodemo.feeder.infinite.sender

import java.util.concurrent.BlockingQueue

import kafka.producer.{KeyedMessage, Producer}
import org.insightedge.geodemo.common.kafkaMessages.{OrderEvent, PickupEvent}
import play.api.libs.json.Json

/**
  * @author Vitaliy_Zinchenko
  */
class PickupSender(pickups: BlockingQueue[PickupEvent],
                   producer: Producer[String, String]) extends Thread {
  override def run(): Unit = {
    while (true) {
      val order = pickups.take()
      send(order)
    }
  }

  private def send(pickup: PickupEvent): Unit = {
    println(s"Sending pickup - $pickup")
    implicit val locationWrites = Json.writes[PickupEvent]
    producer.send(new KeyedMessage[String, String]("pickups", Json.toJson(pickup).toString))
  }
}
