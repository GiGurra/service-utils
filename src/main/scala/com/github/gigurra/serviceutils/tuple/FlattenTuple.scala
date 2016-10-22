package com.github.gigurra.serviceutils.tuple

/**
  * Created by kjolh on 12/20/2015.
  */
object FlattenTuple {

  def apply[T](t: T): T = t

  def apply[T1, T2](ts: (T1, T2)): (T1, T2) = ts

  def apply[T1, T2, T3](ts: ((T1, T2), T3)): (T1, T2, T3) = (ts._1._1, ts._1._2, ts._2)

  def apply[T1, T2, T3, T4](ts: (((T1, T2), T3), T4)): (T1, T2, T3, T4) = {
    val t123 = apply[T1, T2, T3](ts._1)
    (t123._1, t123._2, t123._3, ts._2)
  }

  def apply[T1, T2, T3, T4, T5](ts: ((((T1, T2), T3), T4), T5)): (T1, T2, T3, T4, T5) = {
    val t1234 = apply[T1, T2, T3, T4](ts._1)
    (t1234._1, t1234._2, t1234._3, t1234._4, ts._2)
  }

}
