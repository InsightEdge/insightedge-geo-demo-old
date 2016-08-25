# insightedge-geo-demo
Geospatial API demo: Taxi dynamic price calculation

### Running demo on a local machine

1. Launch InsightEdge `./sbin/insightedge.sh --mode demo`

2. Launch Kafka. Set `KAFKA_HOME` env var with `export KAFKA_HOME=<path/to/kafka>` and then run `./scripts/start-local.sh`

3. Build fat jars `./scripts/build-jars.sh`

4. Launch Feeder `java -jar target/feeder.jar`. Alternatively you can run from IDE, see `org.insightedge.geodemo.feeder.Feeder`

5. Submit InsightEdge processing from InsightEdge directory `./bin/insightedge-submit --class org.insightedge.geodemo.processing.DymanicPriceProcessor --master spark://127.0.0.1:7077 /path/to/insightedgeProcessing.jar spark://127.0.0.1:7077`. Alternatively you can run from IDE with Embedded Spark, see `org.insightedge.geodemo.processing.DymanicPriceProcessor`.

6. Launch web app with `./scripts/start-web.sh`

7. Open `http://localhost:9000`