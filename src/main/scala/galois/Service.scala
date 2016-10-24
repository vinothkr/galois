package galois

import java.util
import java.util.{UUID, Properties}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.config.{ConfigFactory, Config}

import galois.serde.SerDe._
import galois.store.{MetricRequest, MetricStore, RocksStore}
import org.apache.kafka.clients.consumer.KafkaConsumer
import galois.JacksonJsonSupport._

case class Result(key: Key, value: Any)

class Settings(config: Config) {
  val zookeeper = config.getString("kafka.zookeeper")
  val group = config.getString("kafka.group")
  val topic = config.getString("kafka.topic")
  val bootstrap = config.getString("kafka.bootstrap")
  val host = config.getString("http.host")
  val port = config.getInt("http.port")
}

object Service {
  implicit val actorSystem = ActorSystem("galois")
  implicit val materializer = ActorMaterializer()
  

  def main(args: Array[String]): Unit = {
    val settings = new Settings(ConfigFactory.load())
    val store = new RocksStore[Metric.Any]()(kryoInjection)
    val options = new Properties(){
      put("zookeeper.connect", settings.zookeeper)
      put("group.id", settings.group)
      put("bootstrap.servers", settings.bootstrap)
      put("auto.commit.enable", "false")
      put("auto.offset.reset", "earliest")
    }

    val consumer = new KafkaConsumer(options, deserializer(kryoInjection[Long]), deserializer(kryoInjection[Metric.Any]))
    consumer.subscribe(util.Arrays.asList(settings.topic))

    val metricStore: MetricStore = new MetricStore(store)
    val indexer = new Indexer(metricStore, consumer, UUID.randomUUID().toString)
    new Thread(indexer).start()

    val client: GaloisClient = GaloisClient(GaloisConfig(settings.bootstrap, settings.topic, 10))
    client.send(Metric(Key("test"), Average(10)))
    client.flush

    Http().bindAndHandle(Route.handlerFlow(routes(metricStore)), settings.host, settings.port)
  }
  
  def routes(store: MetricStore) = {
    path("metrics"){
      get {
        parameters('name, 'tags.*, 'start.as[Long]?, 'end.as[Long]? ) { (name, tags, start, end) =>
          complete {
            store.metricFor(MetricRequest(name, tags, start, end))
              .map(metric => Result(metric.key, metric.value.show))
          }
        }
      }
    } 
  }
}
