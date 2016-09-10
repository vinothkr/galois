package galois.store

import com.twitter.bijection.Injection
import org.rocksdb.{WriteBatch, WriteOptions, Options, RocksDB}


class RocksStore[V] (implicit val valueSerDe: Injection[V, Array[Byte]]) extends Store[V]{
  val options = new Options().setCreateIfMissing(true)
  lazy val db = RocksDB.open(options,"./db")
  override def get(key: String): Option[V] = Option(db.get(key.getBytes)).map((valueSerDe.invert _).andThen(_.get))

  override def put(key: String, value: V): Unit = db.put(key.getBytes, valueSerDe.apply(value))

  override def batchPut(batch:Batch[V]) = {
    val writeBatch = new WriteBatch()
    batch.items.foreach(item => writeBatch.put(item._1.getBytes, valueSerDe(item._2)))
    db.write(new WriteOptions(), writeBatch)
  }
}
