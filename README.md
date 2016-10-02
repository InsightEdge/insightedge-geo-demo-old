# insightedge-geo-demo
Geospatial API demo: Taxi dynamic price calculation

### Running demo on a local machine

1. Get into demo folder `cd path/to/insightedge-geo-demo`

2. Set INSIGHTEDGE_HOME env variable

```bash
    export INSIGHTEDGE_HOME="path/to/insightedge"
```

3. Launch InsightEdge `$INSIGHTEDGE_HOME/sbin/insightedge.sh --mode demo`

4. Launch Kafka. Set `KAFKA_HOME` env var with `export KAFKA_HOME=<path/to/kafka>` and then run `./scripts/start-local.sh`

5. Build fat jars `./scripts/build-jars.sh`

6. Launch Feeder `java -classpath target/feeder.jar org.insightedge.geodemo.feeder.infinite.Feeder > target/feeder.out 2>&1 &`. Alternatively you can run from IDE, see `org.insightedge.geodemo.feeder.Feeder`

7. Submit InsightEdge processing from InsightEdge directory 
`$INSIGHTEDGE_HOME/bin/insightedge-submit --class org.insightedge.geodemo.processing.DymanicPriceProcessor --master spark://127.0.0.1:7077 ./target/insightedgeProcessing.jar spark://127.0.0.1:7077 > target/processing.out 2>&1 &`. 
Alternatively you can run from IDE with Embedded Spark, see `org.insightedge.geodemo.processing.DymanicPriceProcessor`.

8. Launch web app with `./scripts/start-web.sh`

9. Open `http://localhost:9000`