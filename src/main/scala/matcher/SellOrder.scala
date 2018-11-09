package matcher

import akka.actor.ActorRef

case class SellOrder(client: Client, abcd: Char, price: Int, quantity: Int) extends Order {

  override def getTransactionsAndEffects(optCounterpart: Option[(Client, List[Client])]): (List[UpdateClientState], List[UpdateStock]) = optCounterpart match {
    case Some((buyer, newListOfBuyers)) =>
      (
        List(
          UpdateClientStateStock(client, abcd, -quantity),
          UpdateClientStateStock(buyer, abcd, +quantity),
          UpdateClientStateMoney(client, +quantity * price)
        )
        ,
        List(UpdateStockWithNewList('b', price, quantity, newListOfBuyers))
      )
    case None =>
      (
        List(UpdateClientStateStock(client, abcd, -quantity))
        ,
        List(UpdateStockWithNewClient('s', price, quantity, client))
      )
  }
}
