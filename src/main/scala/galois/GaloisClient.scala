package galois

import java.util.Properties

import galois.serde.SerDe._
import org.apache.kafka.clients.producer.{RecordMetadata, Callback, KafkaProducer, ProducerRecord}

case class GaloisConfig(bootstrap: String, topic: String, noOfPartitions: Int = 1)

class GaloisClient(producer: KafkaProducer[Long, Metric.Any], config: GaloisConfig) {
  def send[T <: Aggregate[T,_]](metric: Metric[T, _]): Unit = {
    producer.send(new ProducerRecord[Long,Metric.Any](config.topic, 0, 0, metric.asInstanceOf[Metric.Any]), new Callback {
      override def onCompletion(recordMetadata: RecordMetadata, e: Exception): Unit = {
        println("Sent" + e + recordMetadata)
      }
    })
  }

  def flush = producer.flush()
}

object GaloisClient {
  def apply(config: GaloisConfig) = {
    lazy val props = new Properties() {
      put("bootstrap.servers", config.bootstrap)
      put("acks", "all")
    }
    val producer = new KafkaProducer[Long, Metric.Any](props, serializer(kryoInjection[Long]), serializer(kryoInjection[Metric.Any]))
    new GaloisClient(producer, config)
  }
}
