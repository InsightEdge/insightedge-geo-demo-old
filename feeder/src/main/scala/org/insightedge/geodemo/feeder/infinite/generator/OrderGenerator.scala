package org.insightedge.geodemo.feeder.infinite.generator

import java.io.{BufferedReader, InputStreamReader}
import java.util.concurrent.BlockingQueue

import com.github.tototoshi.csv.CSVReader
import org.insightedge.geodemo.common.kafkaMessages.OrderEvent
import org.insightedge.geodemo.feeder.Utils

import scala.util.Random

/**
  * @author Vitaliy_Zinchenko
  */
class OrderGenerator(orders: BlockingQueue[OrderEvent],
                     maxWaitTime: Int) extends Thread {

  private val locations = getLocations()

  override def run(): Unit = {
    while(true) {
      val waitTime = Random.nextInt(maxWaitTime)
      val locationPosition = Random.nextInt(locations.size)

      val (lon, lat) = locations(locationPosition)

      val orderEvent: OrderEvent = OrderEvent(Utils.uuid(), System.currentTimeMillis(), lat.toDouble, lon.toDouble)
      orders.put(orderEvent)

      println(s"Generated order $orderEvent. Wait $waitTime")

      Thread.sleep(waitTime)
    }
  }

  private def getLocations(): List[(String, String)] = {
    println("Retrieving locations...")
    val source = new BufferedReader(new InputStreamReader(this.getClass.getClassLoader.getResourceAsStream("uber-raw-data-apr14.csv")))
    val reader = CSVReader.open(source)
    val rows = reader.iterator
    rows.next()
    val locations = rows.map{case List(date, lat, lon, _) => (lat, lon)}.toList
    println(s"Retrieved ${locations.size} locations.")
    locations
  }

}
