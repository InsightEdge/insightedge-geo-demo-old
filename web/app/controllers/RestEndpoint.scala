package controllers

import java.util.{Properties, UUID}

import com.j_spaces.core.client.SQLQuery
import kafka.producer.{KeyedMessage, Producer, ProducerConfig}
import org.insightedge.geodemo.common.gridModel.{NewOrder, OrderRequest, OrderStatus}
import model.OrderSubmit
import org.insightedge.geodemo.common.kafkaMessages.{OrderEvent, PickupEvent}
import org.openspaces.core.GigaSpaceConfigurer
import org.openspaces.core.space.SpaceProxyConfigurer
import org.openspaces.spatial.shapes.Point
import play.api.libs.json._
import play.api.mvc._

object RestEndpoint extends Controller {

  val grid = {
    val spaceConfigurer = new SpaceProxyConfigurer("insightedge-space").lookupGroups("insightedge").lookupLocators("127.0.0.1:4174")
    new GigaSpaceConfigurer(spaceConfigurer).create()
  }

  implicit val pointWrites = new Writes[Point] {
    override def writes(p: Point): JsValue = Json.obj("x" -> p.getX, "y" -> p.getY)
  }
  implicit val orderStatusWrites = new Writes[OrderStatus] {
    override def writes(s: OrderStatus): JsValue = JsString(s.getClass.getSimpleName)
  }
  val fullOrderWrites = Json.writes[OrderRequest]
  val shortOrderWrites = new Writes[OrderRequest] {
    override def writes(r: OrderRequest): JsValue = Json.obj("id" -> r.id, "location" -> r.location)
  }
  val ordersListWrites = Writes.list[OrderRequest](shortOrderWrites)

  implicit val orderSubmitReads = Json.reads[OrderSubmit]
  implicit val orderEventWrites = Json.writes[OrderEvent]
  implicit val pickupEventWrites = Json.writes[PickupEvent]

  /**
    * @return all current orders, in a short format
    */
  def allOrders = Action { implicit request =>
    val query = new SQLQuery[OrderRequest](classOf[OrderRequest], "status = ?", NewOrder)
    val orders = grid.readMultiple[OrderRequest](query).toList
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

  /**
    * Submits a new order
    *
    * @return the generated id of the new order
    */
  def createOrder = Action(parse.json) { request =>
    parseJson(request) { order: OrderSubmit =>
      val id = uuid()
      val event = OrderEvent(id, System.currentTimeMillis(), order.latitude, order.longitude)
      send(Json.toJson[OrderEvent](event).toString(), "orders")
      Created(id)
    }
  }

  /**
    * Submits a pickup event for the order with given id
    */
  def removeOrderById(id: String) = Action { implicit request =>
    val event = PickupEvent(id, System.currentTimeMillis())
    send(Json.toJson[PickupEvent](event).toString(), "pickups")
    Ok
  }


  private def parseJson[R](request: Request[JsValue])(block: R => Result)(implicit reads: Reads[R]): Result = {
    request.body.validate[R](reads).fold(
      valid = block,
      invalid = e => {
        val error = e.mkString
        BadRequest(error)
      }
    )
  }

  // hardcoded to simplify the demo code
  lazy val kafkaConfig = {
    val props = new Properties()
    props.put("metadata.broker.list", "localhost:9092")
    props.put("serializer.class", "kafka.serializer.StringEncoder")
    props
  }
  lazy val producer = new Producer[String, String](new ProducerConfig(kafkaConfig))

  private def send(message: String, topic: String) = producer.send(new KeyedMessage[String, String](topic, message))

  private def uuid(): String = UUID.randomUUID.toString

}