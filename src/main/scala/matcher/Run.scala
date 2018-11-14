package matcher

import java.nio.file.Paths

object Run extends App {

  val initialState: Iterator[ClientState] = getInitialStateFromResources("clients.txt")

  val orders: Iterator[Order] = getOrdersFromFileInResources("orders.txt")

  val result: String = StockSession(initialState, orders).result

  writeToFile(Paths.get("result.txt").toFile,result)

}



