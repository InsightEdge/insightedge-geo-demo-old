package org.insightedge.geodemo.feeder

import scala.collection.mutable.ArrayBuffer

/**
  * Reads batches of elements based on given condition.
  */
class BatchIterator[T](iterator: Iterator[T]) {

  private var buffer: Option[T] = None

  def hasNext: Boolean = buffer.isDefined || iterator.hasNext

  def peek(): T = buffer.getOrElse {
    val v = iterator.next()
    buffer = Some(v)
    v
  }

  def next(): T = buffer match {
    case Some(v) => buffer = None; v
    case None => iterator.next()
  }

  def nextBatch(condition: T => Boolean): Seq[T] = {
    buffer match {
      case Some(t) =>
        if (condition.apply(t)) {
          buffer = None
          t +: readNextBatch(condition)
        } else {
          Seq()
        }
      case None => readNextBatch(condition)
    }
  }

  private def readNextBatch(condition: T => Boolean): Seq[T] = {
    val matched = ArrayBuffer.empty[T]
    while (iterator.hasNext && buffer.isEmpty) {
      iterator.next() match {
        case n if condition.apply(n) => {println(s"condition ${n}<vt=${condition.apply(n)} "); matched += n}
        case n => {println(s"write to buffer ${n}"); buffer = Some(n)}
      }
    }
    matched
  }

}

object BatchIterator {

  def apply[T](iterator: Iterator[T]): BatchIterator[T] = new BatchIterator(iterator)

}