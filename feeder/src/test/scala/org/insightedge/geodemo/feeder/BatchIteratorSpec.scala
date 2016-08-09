package org.insightedge.geodemo.feeder

import org.scalatest.{FunSuite, Matchers}

class BatchIteratorSpec extends FunSuite with Matchers {

  test("iterator should read next batch from the start") {
    val iterator = BatchIterator(List(1, 2, 3, 4, 5).iterator)
    iterator.nextBatch(i => i < 4) shouldEqual Seq(1, 2, 3)
  }

  test("iterator should read two batches") {
    val iterator = BatchIterator(List(1, 2, 3, 4, 5).iterator)
    iterator.nextBatch(i => i < 4) shouldEqual Seq(1, 2, 3)
    iterator.nextBatch(i => i < 6) shouldEqual Seq(4, 5)
    iterator.hasNext shouldBe false
  }

  test("iterator should peek and read batch") {
    val iterator = BatchIterator(List(1, 2, 3, 4, 5).iterator)
    iterator.peek() should equal(1)
    iterator.nextBatch(_ => true) shouldEqual Seq(1, 2, 3, 4, 5)
    iterator.hasNext shouldBe false
  }

  test("iterator should read batch and next element") {
    val iterator = BatchIterator(List(1, 2, 3, 4, 5).iterator)
    iterator.nextBatch(i => i < 4) shouldEqual Seq(1, 2, 3)
    iterator.hasNext shouldBe true
    iterator.next shouldEqual 4
    iterator.nextBatch(_ => true) shouldEqual Seq(5)
  }

}
