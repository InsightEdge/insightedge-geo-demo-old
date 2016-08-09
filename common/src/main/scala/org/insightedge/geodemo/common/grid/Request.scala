package org.insightedge.geodemo.common.grid

import java.util.{Collections, List => JavaList}

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
                    var longitude: Double,

                    @BeanProperty
                    var nearRequestsIds: JavaList[String]

                  ) {

  def this() = this(null, 0L, 0, 0, Collections.emptyList())

}