package galois

import com.twitter.algebird._

import scala.collection.immutable.HashMap

trait Aggregate[T <: Aggregate[_, _], K] { self: T =>
  def plus(another: T): T

  def show: K
}

case class Count(value: Long = 1) extends Aggregate[Count, Long] {
  def plus(another: Count) = Count(value + another.value)

  def show = value
}

case class Average(total: Double, count: Long) extends Aggregate[Average, Double] {
  def plus(another: Average) = Average(total + another.total, count + another.count)

  def show = total / count
}

object Average {
  def apply(value: Double): Average = Average(value, 1)
}

case class UniqueCount(hll: HLL) extends Aggregate[UniqueCount, Long] {
  def plus(another: UniqueCount) = UniqueCount(hll + another.hll)

  def show = hll.approximateSize.estimate
}

object UniqueCount {
  def apply(value: String, bits: Int = 14): UniqueCount = {
    val monoid: HyperLogLogMonoid = new HyperLogLogMonoid(bits)
    UniqueCount(monoid.create(value.getBytes))
  }
}

case class By[T <: Aggregate[T, K], K, G](values: HashMap[G, T with Aggregate[T, K]]) extends Aggregate[By[T, K, G], Map[G, K]] {
  override def plus(another: By[T, K, G]): By[T, K, G] = By(values.merged(another.values) { case ((k1, v1), (k2, v2)) => (k1, v1 plus v2) })

  override def show: Map[G, K] = values.map { case (k, v) => k -> v.show }
}

case class Pair[F <: Aggregate[F,FV], S <: Aggregate[S, SV], FV, SV](f: F with Aggregate[F, FV], s: S with Aggregate[S, SV]) extends Aggregate[Pair[F,S,FV,SV], (FV, SV)] {
  override def plus(another: Pair[F, S, FV, SV]): Pair[F, S, FV, SV] = Pair[F,S,FV,SV](f plus (another.f), s plus (another.s))

  override def show: (FV, SV) = ???
}

object By {
  //Neat little trick for type inference to work - http://stackoverflow.com/questions/39418173/why-is-this-scala-code-not-infering-type
  def apply[T <: Aggregate[T, K], K, G](key: G, value: T with Aggregate[T, K]): By[T, K, G] = By[T, K, G](HashMap(key -> value))
}

