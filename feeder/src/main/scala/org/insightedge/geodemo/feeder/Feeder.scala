package org.insightedge.geodemo.feeder

import java.io.{BufferedReader, InputStreamReader}
import java.lang.System.currentTimeMillis
import java.util.{Properties, UUID}

import com.github.tototoshi.csv.CSVReader
import kafka.producer.{KeyedMessage, Producer, ProducerConfig}
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.Json

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._

object Feeder extends App {

  case class RequestEvent(id: String, time: Long, latitude: Double, longitude: Double)

  case class PickupEvent(requestId: String, time: Long)

  run()

  def run() = {
    // time between taxi request and pickup
    val clientWaitTime = 10.minutes.toMillis
    // events will be populated N times faster
    val simulationSpeedupFactor = 60
    // update interval for the simulation loop
    val simulationRate = 1.second.toMillis
    // simulation start
    val realStartTime = currentTimeMillis()

    val source = new BufferedReader(new InputStreamReader(this.getClass.getClassLoader.getResourceAsStream("uber-raw-data-apr14.csv")))
    val reader = CSVReader.open(source)

    val rows = reader.iterator
    // skip header
    if (rows.hasNext) rows.next()

    val events = BatchIterator(rows.map {
      case List(date, lat, lon, _) => RequestEvent(uuid(), toTime(date), lat.toDouble, lon.toDouble)
    })

    // get first event time
    val virtualStartTime = if (events.hasNext) events.peek().time else 0L

    val futurePickups = new ArrayBuffer[PickupEvent]

    while (events.hasNext) {
      val virtualTime = virtualStartTime + (currentTimeMillis() - realStartTime) * simulationSpeedupFactor

      // find requests to populate, append generated ID
      val requestEvents = events.nextBatch(r => r.time < virtualTime)

      // append current requests as future pickups, clientWaitTime later
      futurePickups ++= requestEvents.map(r => PickupEvent(r.id, r.time + clientWaitTime))

      // find pickups to populate
      val pickupEvents = futurePickups.filter(p => p.time < virtualTime)
      futurePickups.remove(0, pickupEvents.size)

      print(
        s"""
           |$virtualTime:
           |  requests: ${requestEvents.size}
           |  pickups:  ${pickupEvents.size}
           |  current:  ${futurePickups.size}
         """.stripMargin)

      implicit val locationWrites = Json.writes[RequestEvent]
      implicit val driverWrites = Json.writes[PickupEvent]
      requestEvents.foreach(r => send(Json.toJson(r).toString, "requests"))
      pickupEvents.foreach(p => send(Json.toJson(p).toString, "pickups"))

      Thread.sleep(simulationRate)
    }

    producer.close()
  }

  lazy val dateFormatter = DateTimeFormat.forPattern("MM/DD/YYYY HH:mm:ss")

  def toTime(string: String): Long = dateFormatter.parseMillis(string)

  def uuid(): String = UUID.randomUUID.toString

  // hardcoded to simplify the demo code
  lazy val kafkaConfig = {
    val props = new Properties()
    props.put("metadata.broker.list", "localhost:9092")
    props.put("serializer.class", "kafka.serializer.StringEncoder")
    props
  }
  lazy val producer = new Producer[String, String](new ProducerConfig(kafkaConfig))

  def send(message: String, topic: String) = {
    producer.send(new KeyedMessage[String, String](topic, message))
  }

}