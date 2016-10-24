package galois.aggregates

case class Average(total: Double, count: Long) extends Aggregate[Average, Double] {
  def plus(another: Average) = Average(total + another.total, count + another.count)

  def show = total / count
}

object Average {
  def apply(value: Double): Average = Average(value, 1)
}

