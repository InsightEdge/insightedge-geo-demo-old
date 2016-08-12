package org.insightedge.geodemo.common

object restMessages {

  /**
    * Transfer object for order submit
    *
    * @param latitude  the latitude of order coordinates
    * @param longitude the longitude of order coordinates
    */
  case class OrderSubmit(latitude: Double, longitude: Double)

}
