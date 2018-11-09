package matcher

trait Order {
  def client: Client

  def abcd: Char

  def price: Int

  def quantity: Int

  def getTransactionsAndEffects(optCounterpart: Option[(Client, List[Client])]): (List[UpdateClientState], List[UpdateStock])
}