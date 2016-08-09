package org.insightedge.geodemo.common.grid

import java.util.{Collections, List => JavaList}

import org.insightedge.scala.annotation.SpaceId

import scala.beans.BeanProperty

/**
  * Model object stored in the Data Grid
  *
  * @param id              the unique id of the request
  * @param time            the timestamp of request creation
  * @param latitude        the latitude of request coordinates
  * @param longitude       the longitude of request coordinates
  * @param nearRequestsIds the list of detected nearby requests
  */
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