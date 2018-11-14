package matcher

case class BuyOrder(client: Client, ticker: Char, price: Int, quantity: Int) extends Order {
  override def getTransactionsAndEffects(optCounterpart: Option[(Client, List[Client])]): (List[UpdateClientState], List[UpdateStock]) = optCounterpart match {
    case Some((seller, newListOfSellers)) =>
      (
        List(
          UpdateClientStateMoney(client, -quantity * price),
          UpdateClientStateMoney(seller, +quantity * price),
          UpdateClientStateStock(client, ticker, +quantity)
        )
        ,
        List(UpdateStockWithNewList('s', price, quantity, newListOfSellers))
      )
    case None =>
      (
        List(UpdateClientStateMoney(client, -quantity * price))
        ,
        List(UpdateStockWithNewClient('b', price, quantity, client))
      )
  }
}