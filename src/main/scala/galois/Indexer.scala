package galois

import com.twitter.bijection.Bijection
import galois.store.{MetricStore, Batch, Store}
import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer}

import scala.annotation.tailrec
import scala.collection.JavaConversions._

case class IndexKey(key: Key, partition: String)

object IndexKey {
  val keyToString = new Bijection[IndexKey, String] {
    override def apply(value: IndexKey): String =
      (value.key.name :: value.key.tags ++ List(value.key.time, value.partition)).mkString("$")

    override def invert(value: String): IndexKey = {
      val parts = value.split("$")
      val partition = parts.last
      val name = parts.head
      val tags = parts.tail.init.init.toList
      val time = parts.init.last.toLong
      IndexKey(Key(name, tags, time), partition)
    }
  }
}

class Indexer(store: MetricStore,
              consumer: KafkaConsumer[String, Metric.Any],
              partition: String) extends Runnable {

  override def run() = try {
    process
  } finally {
    consumer.close()
  }

  @tailrec
  private final def process: Unit = {
    val records: ConsumerRecords[String, Metric.Any] = consumer.poll(Int.MaxValue)
    var metrics: Map[Key, Metric.Any] = Map.empty

    for (record <- records) {
      val metric: Metric.Any = record.value()
      if (metrics contains metric.key)
        metrics = metrics + (metric.key -> (metrics(metric.key) merge metric))
      else
        metrics = metrics + (metric.key -> metric)
    }

    store.mergeAll(metrics.values, partition)
    consumer.commitSync()
    process
  }
}
