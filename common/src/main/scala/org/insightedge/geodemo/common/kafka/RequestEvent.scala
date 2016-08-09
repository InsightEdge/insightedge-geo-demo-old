package org.insightedge.geodemo.common.kafka

/**
  * Transfer object for request events
  *
  * @param id        the unique id of the request
  * @param time      the timestamp of request creation
  * @param latitude  the latitude of request coordinates
  * @param longitude the longitude of request coordinates
  */
case class RequestEvent(id: String, time: Long, latitude: Double, longitude: Double)