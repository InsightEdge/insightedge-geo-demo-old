package controllers

import com.j_spaces.core.client.SQLQuery
import org.insightedge.geodemo.common.gridModel.Request
import org.openspaces.core.GigaSpaceConfigurer
import org.openspaces.core.space.SpaceProxyConfigurer
import org.openspaces.spatial.shapes.Point
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc._

object RestEndpoint extends Controller {

  val grid = {
    val spaceConfigurer = new SpaceProxyConfigurer("insightedge-space").lookupGroups("insightedge").lookupLocators("127.0.0.1:4174")
    new GigaSpaceConfigurer(spaceConfigurer).create()
  }

  implicit val pointWrites = new Writes[Point] {
    override def writes(p: Point): JsValue = Json.obj("x" -> p.getX, "y" -> p.getY)
  }
  val fullRequestWrites = Json.writes[Request]
  val shortRequestWrites = new Writes[Request] {
    override def writes(r: Request): JsValue = Json.obj("id" -> r.id, "latitude" -> r.location.getX, "longitude" -> r.location.getY)
  }
  val requestListWrites = Writes.list[Request](shortRequestWrites)

  /**
    * @return all current requests, in a short format
    */
  def allRequests = Action { implicit request =>
    val requests = grid.readMultiple[Request](new SQLQuery[Request](classOf[Request], "")).toList
    Ok(Json.toJson(requests)(requestListWrites))
  }

  /**
    * @return full information on the request with specified id
    */
  def requestById(id: String) = Action { implicit request =>
    Option(grid.readById(classOf[Request], id)) match {
      case Some(r) => Ok(Json.toJson(r)(fullRequestWrites))
      case None => NotFound
    }
  }

}