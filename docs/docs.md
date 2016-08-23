# Real-time analytics on a streaming geospatial data: dynamic taxi pricing

## Overview

In this blog post we will create a demo application that runs real-time analytics on a streaming geospatial data.

We take a fundamental **supply and demand** economic model of price determination in a market.
We will compute price in real-time based on the current supply and demand.

Demand refers to how much (quantity) of a product or service is desired by buyers.
The quantity demanded is the amount of a product people are willing to buy at a certain price.
Supply represents how much the market can offer. Price, therefore, is a reflection of supply and demand.

To make our demo even more fun, we consider transportation business domain and taxi companies like Uber or Lyft in particularly.
In taxi services the order requests and available drivers represent the supply and demand data correspondingly.
It is interesting that this data is bound to **geographical location** which introduces additional complexity. Comparing to business areas like
retail, where the product demand is linked to either offline store or a well known list of warehouses, the order requests are geographically distributed.

With services like Uber the fare rates automatically increase, when the taxi demand is higher than drivers around you.
The Uber prices are [surging](https://help.uber.com/h/19572af0-d494-4885-a1ef-1a0d54d0e68f) to ensure reliability and availability for those who agree to pay a bit more.

You may identify the following architectural questions:
- how do we handle the events like order request event or pickup event?
- how do we compute the price accounting the nearby requests? We need an efficient way to execute geospatial queries.
- how can we scale technology to run business in many cities, states or countries?


## Architecture

The following diagram illustrates the application architecture:

![Alt architecture](img/geo-demo-arch-diagram.jpg?raw=true "architecture")

Let's now see how this architecture addresses the key questions we outlined earlier:
- with InsightEdge Geospatial API we are able to efficiently find nearby orders and, therefore, minimize the time required to compute the price.
The efficiency comes from the ability to **index order request location** in the datagrid.
- Kafka allows to handle a **high throughput** of incoming raw events.
Even if the computation layer starts processing slower(say during the peak hour), all the events will be reliably buffered in Kafka. The seamless and proven integration with Spark makes it a good choice for streaming applications.
- InsightEdge Data Grid also plays a role of a serving layer **handling any operational/transactional queries** from web/mobile apps.
- all the components(Kafka and InsightEdge) can **scale out** almost linearly;
- to scale to many cities, we can leverage data locality principle through a full pipeline (Kafka, Spark, Data Grid)
partitioning by the `city` or even with a more gradular geographical grid. In this case the geospatial search query will be limited to a single Data Grid partition. We leave this enhancement out of the scope of the demo.

## Building a demo application

To simulate the taxi orders we took a [csv dataset](https://github.com/fivethirtyeight/uber-tlc-foil-response) with Uber pickups in New York City. The demo application consists of following components:
- feeder application, reads the csv file and sends order and pickup events to Kafka
- InsightEdge processing, a Spark Streaming application that reads from Kafka, computes price and saves to datagrid
- web app, reads orders from datagrid and visualizes them on a map

![Alt demo screenshot](img/demo_screenshot.jpg?raw=true "demo screenshot")

## Coding processing logic with InsightEdge API

Let's see how InsightEdge API used to calculate the price:

```scala
val ordersStream = initKafkaStream(ssc, "orders") // step 1

ordersStream
  .map(message => Json.parse(message).as[OrderEvent]) // step 2
  .transform { rdd =>  // step 3
    val query = "location spatial:within ? AND status = ?"
    val radius = 0.5 * DistanceUtils.KM_TO_DEG
    val queryParamsConstructor = (e: OrderEvent) => Seq(circle(point(e.longitude, e.latitude), radius), NewOrder)
    val projections = Some(Seq("id"))
    rdd.zipWithGridSql[OrderRequest](query, queryParamsConstructor, projections)
  }
  .map { case (e: OrderEvent, nearOrders: Seq[OrderRequest]) => // step 4
    val location = point(e.longitude, e.latitude)
    val nearOrderIds = nearOrders.map(_.id)
    val priceFactor = if (nearOrderIds.length > 3) {
      1.0 + (nearOrderIds.length - 3) * 0.1
    } else {
      1.0
    }
    OrderRequest(e.id, e.time, location, priceFactor, nearOrderIds, NewOrder)
  }
  .saveToGrid() // step 5
```

- step 1: initialize a stream of Kafka `orders` topic
- step 2: parse Kafka message that is in Json format (in real app you may want to use formats like Avro)
- step 3: for every order we find other nonprocessed orders within 0.3 km using InsightEdge's `zipWithGridSql()` function
- step 4: given near orders, we calculate the price with a simple linear function
- step 5: finally we save the order details including price and near order ids into the data grid with `saveToGrid()` function

The full source of the application is available on [github](https://github.com/InsightEdge/insightedge-geo-demo)

## Summary

In this blog post we created a demo application that processes the data stream using InsightEdge geospatial features.

An alternative approach of implementing dynamic price surging can use machine learning clustering algorythms to split order requests into clusters
and calculate if the demand within a cluster is higher than supply. This streaming application saves the cluster details in the datagrid. Then
to find out the price we execute geospatial query on datagrid to find which the cluster the given location belongs to.
