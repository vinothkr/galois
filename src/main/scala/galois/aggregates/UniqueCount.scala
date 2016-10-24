package galois.aggregates

import com.twitter.algebird.{HyperLogLogMonoid, HLL}

trait UniqueCount extends Aggregate[UniqueCount, Long]

case class UniqueCount1(value:String, bits: Int) extends UniqueCount {
  def plus(another: UniqueCount) = another match {
    case other: UniqueCount1  => UniqueCountN(this.toHLL + other.toHLL)
    case UniqueCountN(otherHLL) => UniqueCountN(this.toHLL + otherHLL)
  }

  def toHLL = {
    val monoid: HyperLogLogMonoid = new HyperLogLogMonoid(bits)
    monoid.create(value.getBytes)
  }

  def show = 1
}

case class UniqueCountN(hll: HLL) extends UniqueCount {
  def plus(another: UniqueCount) = another match {
    case UniqueCountN(anotherHll) => UniqueCountN(hll + anotherHll)
    case other:UniqueCount1 => UniqueCountN(other.toHLL + hll)
  }

  def show = hll.approximateSize.estimate
}

object UniqueCount {
  def apply(value: String, bits: Int = 14): UniqueCount = UniqueCount1(value, bits)
}