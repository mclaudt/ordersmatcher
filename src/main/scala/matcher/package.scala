import java.io.{File, FileWriter}

import matcher.Run.res

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


  // DispenserActor
  case class WaitForNResponses(n: Int)


  //Universal

  case class CancelAllOrders()




  //Utils

  def writeToFile(file: File, str: String): Unit = {
    val writer = new FileWriter(file)
    try {
      writer.append(res).append("\n")
    }
    finally {
      writer.close()
    }

  }

}
