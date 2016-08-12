package org.insightedge.geodemo.common

import java.util.{Collections, List => JavaList}

import org.openspaces.spatial.shapes._
import org.insightedge.scala.annotation._

import scala.beans.BeanProperty

object gridModel {

  sealed trait OrderStatus
  case object NewOrder extends OrderStatus
  case object ProcessedOrder extends OrderStatus


  /**
    * Model object stored in the Data Grid
    *
    * @param id              unique id of the order
    * @param time            timestamp of order creation
    * @param location        location of order coordinates
    * @param nearOrderIds list of detected nearby order ids
    */
  case class OrderRequest(

                      @BeanProperty
                      @SpaceId
                      var id: String,

                      @BeanProperty
                      var time: Long,

                      @BeanProperty
                      @SpaceSpatialIndex
                      var location: Point,

                      @BeanProperty
                      var priceFactor: Double,

                      @BeanProperty
                      var nearOrderIds: Seq[String],

                      @BeanProperty
                      var status: OrderStatus

                    ) {

    def this() = this(null, 0L, null, 0, Seq(), null)

  }

}
