package matcher

import org.scalatest.FlatSpec

class StockSessionSpec extends FlatSpec {

  private val client1 = "C1"
  private val client2 = "C2"

  private val emptyStock = Map.empty[Char,Int].withDefaultValue(0)
  private val stockOfOneA = emptyStock + ('A' -> 1)
  private val stockOfTenA = emptyStock + ('A' -> 10)


  "A basic two-client session " should "produce a simple exchange" in {

    val initialState: Iterator[ClientState] = List(
      ClientState(client1, 100),
      ClientState(client2, 0, stockOfOneA)
    ).toIterator

    val orders: Iterator[Order] = List(
      BuyOrder(client1, 'A', 100, 1),
      SellOrder(client2, 'A', 100, 1)
    ).toIterator


    assert(StockSession(initialState, orders).result ==
      s"""$client1	0	1	0	0	0
         |$client2	100	0	0	0	0""".stripMargin)

  }


  it should "prevent orders of client with himself" in {

    val initialState: Iterator[ClientState] = List(
      ClientState(client1, 100),
      ClientState(client2, 100, stockOfOneA)
    ).toIterator

    val orders: Iterator[Order] = List(
      SellOrder(client2, 'A', 100, 1),
      BuyOrder(client2, 'A', 100, 1),
      BuyOrder(client1, 'A', 100, 1)

    ).toIterator

    assert(StockSession(initialState, orders).result ==
      s"""$client1	0	1	0	0	0
         |$client2	200	0	0	0	0""".stripMargin)

  }


  it should "prevent orders which lead to negative account value" in {


    val initialState: Iterator[ClientState] = List(
      ClientState(client1, 100),
      ClientState(client2, 0, stockOfTenA)
    ).toIterator

    val orders: Iterator[Order] = List(
      SellOrder(client2, 'A', 100, 10),
      BuyOrder(client1, 'A', 100, 10)

    ).toIterator

    assert(StockSession(initialState, orders).result ==
      s"""$client1	100	0	0	0	0
         |$client2	0	10	0	0	0""".stripMargin)

  }

}