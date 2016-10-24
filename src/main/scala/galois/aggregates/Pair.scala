package galois.aggregates

case class Pair[F <: Aggregate[F,FV], S <: Aggregate[S, SV], FV, SV](f: F with Aggregate[F, FV], s: S with Aggregate[S, SV]) extends Aggregate[Pair[F,S,FV,SV], (FV, SV)] {
  override def plus(another: Pair[F, S, FV, SV]): Pair[F, S, FV, SV] = Pair[F,S,FV,SV](f plus (another.f), s plus (another.s))

  override def show: (FV, SV) = (f.show, s.show)
}
