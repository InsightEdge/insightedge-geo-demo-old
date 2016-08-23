package org.insightedge.geodemo.common

object kafkaMessages {

  /**
    * Transfer object for pickup events
    *
    * @param orderId the id of the order that is picked up
    * @param time      timestamp of the pickup
    */
  case class PickupEvent(orderId: String, time: Long)

  /**
    * Transfer object for order events
    *
    * @param id        the unique id of the order request
    * @param time      the timestamp of order creation
    * @param latitude  the latitude of order coordinates
    * @param longitude the longitude of order coordinates
    */
  case class OrderEvent(id: String, time: Long, latitude: Double, longitude: Double)

}
