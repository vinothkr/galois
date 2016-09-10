package galois.store

import galois.{IndexKey, Metric}


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
}
