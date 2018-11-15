package matcher

import matcher.ClientStatesActor.{UpdateClientState, UpdateClientStateMoney, UpdateClientStateStock}
import matcher.StockActor.{UpdateStock, UpdateStockWithNewClient, UpdateStockWithNewList}


case class SellOrder(client: Client, ticker: Char, price: Int, quantity: Int) extends Order {

  override def getTransactionsAndEffects(optCounterpart: Option[(Client, List[Client])]): (List[UpdateClientState], List[UpdateStock]) = optCounterpart match {
    case Some((buyer, newListOfBuyers)) =>
      (
        List(
          UpdateClientStateStock(client, ticker, -quantity),
          UpdateClientStateStock(buyer, ticker, +quantity),
          UpdateClientStateMoney(client, +quantity * price)
        )
        ,
        List(UpdateStockWithNewList('b', price, quantity, newListOfBuyers))
      )
    case None =>
      (
        List(UpdateClientStateStock(client, ticker, -quantity))
        ,
        List(UpdateStockWithNewClient('s', price, quantity, client))
      )
  }
}

