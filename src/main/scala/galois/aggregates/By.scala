package galois.aggregates

import scala.collection.immutable.HashMap

case class By[T <: Aggregate[T, K], K, G](values: HashMap[G, T with Aggregate[T, K]]) extends Aggregate[By[T, K, G], Map[G, K]] {
  override def plus(another: By[T, K, G]): By[T, K, G] = By(values.merged(another.values) { case ((k1, v1), (k2, v2)) => (k1, v1 plus v2) })

  override def show: Map[G, K] = values.map { case (k, v) => k -> v.show }
}

object By {
  //Neat little trick for type inference to work - http://stackoverflow.com/questions/39418173/why-is-this-scala-code-not-infering-type
  def apply[T <: Aggregate[T, K], K, G](key: G, value: T with Aggregate[T, K]): By[T, K, G] = By[T, K, G](HashMap(key -> value))
}