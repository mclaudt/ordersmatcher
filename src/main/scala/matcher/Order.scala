package matcher

import matcher.ClientStatesActor.UpdateClientState
import matcher.StockActor.UpdateStock

trait Order {
  def client: Client

  def ticker: Char

  def price: Int

  def quantity: Int

  def getTransactionsAndEffects(optCounterpart: Option[(Client, List[Client])]): (List[UpdateClientState], List[UpdateStock])
}
