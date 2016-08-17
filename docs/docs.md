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
- how do we handle the various events like order request event or pickup event?
- how do we compute the price accounting the nearby requests? We need an efficient way to execute geospatial queries?
- how can we scale technology to run business in many cities, states or countries?


## Architecture

The following diagram illustrates the application architecture:

![Alt architecture](img/geo-demo-arch-diagram.jpg?raw=true "architecture")

Let's now see how this architecture addresses the key questions we outlined earlier:
- with InsightEdge Geospatial API we are able to efficiently find nearby orders and, therefore, minimize the time required to compute the price.
The efficiency comes from the ability to index order request location in the datagrid.
- Kafka allows to handle a huge throughput of incoming raw events.
Even if the computation layer starts processing slower(say during the peak hour), all the events will reliably buffered in Kafka. The seamless and proven integration with Spark makes it a good choice for streaming applications.
- InsightEdge datagrid also plays a role of a serving layer handling any operational/transactional queries from web/mobile apps.
- all the components(Kafka and InsightEdge) can scale out almost linearly;

## Building a demo application

To simulate the taxi orders we took a [csv dataset](https://github.com/fivethirtyeight/uber-tlc-foil-response) with Uber pickups in New York City. The demo application consists of following components:
- feeder application, reads the csv file and sends order request and pickup events to Kafka
- InsightEdge processing, a Spark Streaming application that reads from Kafka, computes price and saves to datagrid
- web app, reads orders from datagrid and visualizes them on a map




