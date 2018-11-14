package matcher

import IOUtils.{getInitialStateFromResources, getOrdersFromFileInResources}

class ProcessInputFilesFromResources(val initialStateFileName:String, val ordersFileName:String, val resultFileName:String) {

  val initialState: Iterator[ClientState] = getInitialStateFromResources(initialStateFileName)

  val orders: Iterator[Order] = getOrdersFromFileInResources(ordersFileName)

  val result: String = StockSession(initialState, orders).result

  def run(): Unit = writeToFileByName(resultFileName,result)
}
