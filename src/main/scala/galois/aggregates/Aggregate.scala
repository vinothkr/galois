package galois.aggregates

trait Aggregate[T <: Aggregate[_, _], K] { self: T =>
  def plus(another: T): T

  def show: K
}

