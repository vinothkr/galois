package galois.store

import galois.{Key, IndexKey, Metric}


case class MetricRequest(name: String, tags: Iterable[String], startTime: Option[Long], endTime: Option[Long]) {
  def startKey = (name :: tags.toList).mkString("$") + "$" + startTime.map(_.toString).getOrElse("")
  def endKey = (name :: tags.toList).mkString("$") + "$" + endTime.map(_.toString).getOrElse("") + "~"
}

class MetricStore(store: Store[Metric.Any]) {

  def mergeAll(metrics: Iterable[Metric.Any], partition: String) = {
    var batch = List.empty[(String, Metric.Any)]
    for (metric <- metrics) {

      val indexKey = IndexKey.keyToString(IndexKey(metric.key, partition))
      val left = store.get(indexKey)
      if (left isDefined) batch = (indexKey -> left.get.merge(metric)) :: batch
      else batch = (indexKey -> metric) :: batch
    }
    store.batchPut(Batch(batch))
  }

  def metricFor(request: MetricRequest): List[Metric[_,_]] = {
    store.scan(request.startKey, request.endKey)
      .map(_._2)
      .toList
      .groupBy(_.key)
      .map{case (key, values) => values.reduce(_ merge _)}
      .toList
  }
}
