package org.insightedge.geodemo.common.kafka

/**
  * Transfer object for pickup events
  *
  * @param requestId the id of the request that is picked up
  * @param time      timestamp of the pickup
  */
case class PickupEvent(requestId: String, time: Long)