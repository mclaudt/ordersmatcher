import java.io.{File, FileWriter}
import java.nio.file.Paths

package object matcher {

  type Client = String


  // ClientStateActor

  case class ClientState(name: Client, money: Int, stock: Map[Char, Int] = Map.empty.withDefaultValue(0))

  sealed trait UpdateClientState

    case class UpdateClientStateMoney(name: Client, money: Int) extends UpdateClientState

    case class UpdateClientStateStock(name: Client, ticker: Char, quantity: Int) extends UpdateClientState

  sealed trait UpdateStock

    case class UpdateStockWithNewList(bs: Char, price: Int, quantity: Int, newListOfClients: List[Client]) extends UpdateStock

    case class UpdateStockWithNewClient(bs: Char, price: Int, quantity: Int, client: Client) extends UpdateStock

  case class PleaseApproveThisOrder(oder:Order)

  sealed trait OrderApproveResult
  case class OrderApproved(io: Order) extends OrderApproveResult
  case class OrderDenied() extends OrderApproveResult


  // DispenserActor

  case class WaitForNConfirmations(n: Int)


  //Universal

  case class CancelAllOrders()

  case class OperationHasBeenPerformedConfirmation()




  //Utils

  private def writeToFile(file: File, str: String): Unit = {
    val writer = new FileWriter(file)
    try {
      writer.append(str).append("\n")
    }
    finally {
      writer.close()
    }

  }

  def writeToFileByName(fileName:String = "result.txt",result:String): Unit = writeToFile(Paths.get(fileName).toFile,result)

}
