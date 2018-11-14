import java.io.{File, FileWriter}
import scala.io.Source

package object matcher {

  type Client = String


  // ClientStateActor

  case class ClientState(name: Client, money: Int, stock: scala.collection.mutable.Map[Char, Int])

  sealed trait UpdateClientState

    case class UpdateClientStateMoney(name: Client, money: Int) extends UpdateClientState

    case class UpdateClientStateStock(name: Client, abcd: Char, quantity: Int) extends UpdateClientState

  sealed trait UpdateStock

    case class UpdateStockWithNewList(bs: Char, price: Int, quantity: Int, newListOfClients: List[Client]) extends UpdateStock

    case class UpdateStockWithNewClient(bs: Char, price: Int, quantity: Int, client: Client) extends UpdateStock

  case class PleaseApproveThisOrder(oder:Order)

  sealed trait OrderApproveResult
  case class OrderApproved(io: Order) extends OrderApproveResult
  case class OrderDenied() extends OrderApproveResult


  // DispenserActor

  case class WaitForNResponses(n: Int)


  //Universal

  case class CancelAllOrders()




  //Utils

  def writeToFile(file: File, str: String): Unit = {
    val writer = new FileWriter(file)
    try {
      writer.append(str).append("\n")
    }
    finally {
      writer.close()
    }

  }

  def getInitialStateFromResources(fileName:String): Iterator[ClientState] = Source.fromResource(fileName).getLines.map(l => {
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

  def getOrdersFromFileInResources(fileName:String): Iterator[Order] = Source.fromResource(fileName).getLines.map(l => {
    l.split("\t").map(s => s.trim) match {
      case Array(client, bs, abcd, price, quantity) => {

        bs.head match {
          case 'b' => SellOrder(client, abcd.head, price.toInt, quantity.toInt)
          case 's' => BuyOrder(client, abcd.head, price.toInt, quantity.toInt)
        }
      }

    }
  })

}
