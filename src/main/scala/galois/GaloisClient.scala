package galois

import java.net.InetAddress
import java.util.Properties

import galois.aggregates.Aggregate
import galois.serde.SerDe._
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}

case class GaloisConfig(bootstrap: String, topic: String)

class GaloisClient(producer: KafkaProducer[String, Metric.Any], config: GaloisConfig) {
  val hostName = InetAddress.getLocalHost.getCanonicalHostName
  def send[T <: Aggregate[T,_]](metric: Metric[T, _]): Unit = {
    producer.send(new ProducerRecord[String,Metric.Any](config.topic, hostName + System.currentTimeMillis(), metric.asInstanceOf[Metric.Any]), new Callback {
      override def onCompletion(recordMetadata: RecordMetadata, e: Exception): Unit = {
        if(e != null) e.printStackTrace()
      }
    })
  }

  def flush() = producer.flush()
}

object GaloisClient {
  def apply(config: GaloisConfig) = {
    lazy val props = new Properties() {
      put("bootstrap.servers", config.bootstrap)
      put("acks", "all")
    }
    val producer = new KafkaProducer[String, Metric.Any](props, serializer(kryoInjection[String]), serializer(kryoInjection[Metric.Any]))
    new GaloisClient(producer, config)
  }
}
