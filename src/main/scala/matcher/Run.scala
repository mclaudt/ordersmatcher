package matcher

import java.nio.file.Paths
import scala.io.Source

object Run extends App {


  val initialState: Iterator[ClientState] = Source.fromResource("clients.txt").getLines.map(l => {
    l.split("\t").map(s => s.trim) match {
      case Array(name, money, a, b, c, d) =>
        ClientState(
          name,
          money.toInt,
          scala.collection.mutable.Map(
            'A' -> a.toInt,
            'B' -> b.toInt,
            'C' -> c.toInt,
            'D' -> d.toInt
          )
        )
    }
  })



  val orders = Source.fromResource("orders.txt").getLines.map(l => {
    l.split("\t").map(s => s.trim) match {
      case Array(client, bs, abcd, price, quantity) => {

        bs.head match {
          case 'b' => SellOrder(client, abcd.head, price.toInt, quantity.toInt)
          case 's' => BuyOrder(client, abcd.head, price.toInt, quantity.toInt)
        }
      }

    }
  })

  val res = StockSession(initialState, orders).result

  println(res)

  writeToFile(Paths.get("result.txt").toFile,res)

}



