import org.scalatest.FlatSpec
import matcher._

class StockSessionSpec extends FlatSpec {

  "A basic two-client session " should "produce a simple exchange" in {

    val initialState: Iterator[ClientState] = List(
      ClientState("C1", 100, scala.collection.mutable.Map.empty),
      ClientState("C2", 0, scala.collection.mutable.Map('A' -> 1))
    ).toIterator

    val orders: Iterator[Order] = List(
      BuyOrder("C1", 'A', 100, 1),
      SellOrder("C2", 'A', 100, 1)
    ).toIterator


    assert(StockSession(initialState, orders).result ==
      """C1	0	1	0	0	0
        |C2	100	0	0	0	0""".stripMargin)

  }


  "Session " should "prevent orders of client with himelf" in {

    val initialState: Iterator[ClientState] = List(
      ClientState("C1", 100, scala.collection.mutable.Map.empty),
      ClientState("C2", 100, scala.collection.mutable.Map('A' -> 1))
    ).toIterator

    val orders: Iterator[Order] = List(
      SellOrder("C2", 'A', 100, 1),
      BuyOrder("C2", 'A', 100, 1),
      BuyOrder("C1", 'A', 100, 1)

    ).toIterator

    assert(StockSession(initialState, orders).result ==
      """C1	0	1	0	0	0
        |C2	200	0	0	0	0""".stripMargin)

  }


  "Session " should "prevent orders which lead to negative account value" in {


    val initialState: Iterator[ClientState] = List(
      ClientState("C1", 100, scala.collection.mutable.Map.empty),
      ClientState("C2", 0, scala.collection.mutable.Map('A' -> 10))
    ).toIterator

    val orders: Iterator[Order] = List(
      SellOrder("C2", 'A', 100, 10),
      BuyOrder("C1", 'A', 100, 10)

    ).toIterator

    assert(StockSession(initialState, orders).result ==
      """C1	100	0	0	0	0
        |C2	0	10	0	0	0""".stripMargin)

  }

}