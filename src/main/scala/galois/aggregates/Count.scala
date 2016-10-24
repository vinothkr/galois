package galois.aggregates

case class Count(value: Long = 1) extends Aggregate[Count, Long] {
  def plus(another: Count) = Count(value + another.value)

  def show = value
}