package org.insightedge.geodemo.common

import java.util.{Collections, List => JavaList}

import org.openspaces.spatial.shapes._
import org.insightedge.scala.annotation._

import scala.beans.BeanProperty

object gridModel {

  /**
    * Model object stored in the Data Grid
    *
    * @param id              unique id of the request
    * @param time            timestamp of request creation
    * @param location        location of request coordinates
    * @param nearRequestsIds list of detected nearby requests
    */
  case class Request(

                      @BeanProperty
                      @SpaceId
                      var id: String,

                      @BeanProperty
                      var time: Long,

                      @BeanProperty
                      var location: Point,

                      @BeanProperty
                      var nearRequestsIds: Seq[String]

                    ) {

    def this() = this(null, 0L, null, Seq())

  }

}
