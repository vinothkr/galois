package galois

import galois.Metric.Any
import org.apache.kafka.clients.producer.{Callback, ProducerRecord, KafkaProducer}
import org.mockito.ArgumentCaptor
import org.mockito.Mockito._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

class GaloisClientSpec extends FlatSpec {
  val producer = mock(classOf[KafkaProducer[String, Any]])
  val client: GaloisClient = new GaloisClient(producer, GaloisConfig("localhost:9091","galois"))

  "Client" should "produce a producer record with the given metric" in {
    val metric = mock(classOf[Any])
    client.send(metric)

    val captor = ArgumentCaptor.forClass(classOf[ProducerRecord[String, Any]])
    val callbackCaptor = ArgumentCaptor.forClass(classOf[Callback])
    verify(producer).send(captor.capture(), callbackCaptor.capture())

    captor.getValue.topic() should be("galois")
    captor.getValue.value() should be(metric)
  }
}
