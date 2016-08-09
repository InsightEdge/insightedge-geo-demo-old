package org.insightedge.geodemo.processing

import org.insightedge.scala.annotation.SpaceId

import scala.beans.BeanProperty

case class Request(

                    @BeanProperty
                    @SpaceId
                    var id: String,

                    @BeanProperty
                    var time: Long,

                    @BeanProperty
                    var latitude: Double,

                    @BeanProperty
                    var longitude: Double

                  ) {

  def this() = this(null, 0L, 0, 0)

}