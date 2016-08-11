package controllers

import com.j_spaces.core.client.SQLQuery
import org.insightedge.geodemo.common.gridModel.OrderRequest
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
  val fullOrderWrites = Json.writes[OrderRequest]
  val shortOrderWrites = new Writes[OrderRequest] {
    override def writes(r: OrderRequest): JsValue = Json.obj("id" -> r.id, "latitude" -> r.location.getX, "longitude" -> r.location.getY)
  }
  val ordersListWrites = Writes.list[OrderRequest](shortOrderWrites)

  /**
    * @return all current orders, in a short format
    */
  def allOrders = Action { implicit request =>
    val orders = grid.readMultiple[OrderRequest](new SQLQuery[OrderRequest](classOf[OrderRequest], "")).toList
    Ok(Json.toJson(orders)(ordersListWrites))
  }

  /**
    * @return full information on the order with specified id
    */
  def orderById(id: String) = Action { implicit request =>
    Option(grid.readById(classOf[OrderRequest], id)) match {
      case Some(r) => Ok(Json.toJson(r)(fullOrderWrites))
      case None => NotFound
    }
  }

}