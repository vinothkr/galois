package galois

case class Key(name:String, tags:List[String] = List.empty, time:Long = 0)

case class Metric[T <: Aggregate[T,K], K] (key: Key, value: T with Aggregate[T, K]) {
  type Aggr = T with Aggregate[T,K]
  def merge:PartialFunction[Metric.Any, Metric[T, K]] = {
    //Not sure why the type cast is necessary but it cribs
    case other:Metric[T, K] if other.key == this.key  => Metric[T, K](key, value.plus(other.asInstanceOf[Metric[Aggr,K]].value).asInstanceOf[Aggr])
  }
}

object Metric {
  type Any = Metric[M, N] forSome {type N; type M <: Aggregate[M, N]}
}