package org.insightedge.geodemo.feeder.infinite.generator

import java.util
import java.util.concurrent.BlockingQueue

import org.insightedge.geodemo.common.kafkaMessages.{OrderEvent, PickupEvent}

import scala.collection.JavaConversions._
import scala.util.Random

/**
  * @author Vitaliy_Zinchenko
  */
class PickupGenerator(waitingOrders: BlockingQueue[OrderEvent],
                      pickups: BlockingQueue[PickupEvent],
                      maxWaitTime: Int,
                      maxBatchSize: Int) extends Thread {
  override def run(): Unit = {
    while(true) {
      val pickupCount = Random.nextInt(maxBatchSize)
      val waitTime = Random.nextInt(maxWaitTime)
      val buffer = new util.ArrayList[OrderEvent]()
      waitingOrders.drainTo(buffer, pickupCount)

      buffer.foreach{ order =>
        println(s"Creating pickup event from $order")
        pickups.add(PickupEvent(order.id, order.time))
      }

      println(s"Pickup generator created ${buffer.size()} events, Waiting $waitTime")
      Thread.sleep(waitTime)
    }
  }
}
