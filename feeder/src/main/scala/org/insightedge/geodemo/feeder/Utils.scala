package org.insightedge.geodemo.feeder

import java.util.{Properties, UUID}

import kafka.producer.{Producer, ProducerConfig}
import org.joda.time.format.DateTimeFormat

/**
  * @author Vitaliy_Zinchenko
  */
object Utils {

  lazy val dateFormatter = DateTimeFormat.forPattern("MM/DD/YYYY HH:mm:ss")

  def toTime(string: String): Long = dateFormatter.parseMillis(string)

  def uuid(): String = UUID.randomUUID.toString

  def createProducer(): Producer[String, String] = {
    // hardcoded to simplify the demo code
    val props = new Properties()
    props.put("metadata.broker.list", "localhost:9092")
    props.put("serializer.class", "kafka.serializer.StringEncoder")

    new Producer[String, String](new ProducerConfig(props))
  }

}
