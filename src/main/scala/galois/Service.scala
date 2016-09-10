package galois

import java.util
import java.util.Properties

import galois.serde.SerDe._
import galois.store.{MetricStore, RocksStore}
import org.apache.kafka.clients.consumer.KafkaConsumer


object Service {
  def main(args: Array[String]): Unit = {
    val store = new RocksStore[Metric.Any]()(kryoInjection)
    val options = new Properties(){
      put("zookeeper.connect", "localhost:2181")
      put("group.id", "galois-new5")
      put("bootstrap.servers", "localhost:9092")
      put("auto.commit.enable", "false")
      put("auto.offset.reset", "earliest")
    }

    val consumer = new KafkaConsumer(options, deserializer(kryoInjection[Long]), deserializer(kryoInjection[Metric.Any]))
    consumer.subscribe(util.Arrays.asList("galois-test4"))

    val indexer = new Indexer(new MetricStore(store), consumer, "1")
    new Thread(indexer).start()
  }
}
