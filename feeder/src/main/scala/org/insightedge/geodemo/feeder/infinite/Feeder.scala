package org.insightedge.geodemo.feeder.infinite

import java.util.concurrent.LinkedBlockingQueue

import org.insightedge.geodemo.common.kafkaMessages.{OrderEvent, PickupEvent}
import org.insightedge.geodemo.feeder.Utils
import org.insightedge.geodemo.feeder.infinite.generator.{OrderGenerator, PickupGenerator}
import org.insightedge.geodemo.feeder.infinite.sender.{OrderSender, PickupSender}

object Feeder extends App {

  val (maxWaitTime, maxBatchSize) = getArgs()

  val producer = Utils.createProducer()

  val orders = new LinkedBlockingQueue[OrderEvent]()
  val waitingOrders = new LinkedBlockingQueue[OrderEvent]()
  val pickups = new LinkedBlockingQueue[PickupEvent]()

  val orderGenerator = new OrderGenerator(orders, maxWaitTime)
  val pickupGenerator = new PickupGenerator(waitingOrders, pickups, maxWaitTime, maxBatchSize)

  val orderSender = new OrderSender(orders, producer, waitingOrders)
  val pickupSender = new PickupSender(pickups, producer)

  run()

  def run() = {
    orderGenerator.start()
    pickupGenerator.start()

    orderSender.start()
    pickupSender.start()
  }

  private def getArgs(): (Int, Int) = {
    val maxWaitTime: Int = if(args.length >= 1) args(0).toInt else 1000
    val maxBatchSize: Int = if(args.length == 2) args(1).toInt else 10
    (maxWaitTime, maxBatchSize)
  }


}