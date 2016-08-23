# insightedge-geo-demo
Geospatial API demo: Taxi dynamic price calculation

### Running demo on a local machine

1. Launch InsightEdge `./sbin/insightedge.sh --mode demo`

2. Launch Kafka. Set `KAFKA_HOME` env var with `export KAFKA_HOME=<path/to/kafka>` and then run `/scripts/start-local.sh`

3. Launch Feeder. You can either launch from IDE `org.insightedge.geodemo.feeder.Feeder` or build a fat jar with `sbt feeder/assembly` and then launch with `java -jar <path/to/feeder.jar>`

4. Launch InsightEdge processing app. If you want to run from IDE with embedded Spark, see `org.insightedge.geodemo.processing.DymanicPriceProcessor`. Or build fat jar with `sbt insightedgeProcessing/assembly` and submit with `./bin/insightedge-submit --class org.insightedge.geodemo.processing.DymanicPriceProcessor --master spark://127.0.0.1:7077 /path/to/insightedgeProcessing.jar spark://127.0.0.1:7077`

5. Launch web app with `sbt web/run`

6. Open `http://localhost:9000`