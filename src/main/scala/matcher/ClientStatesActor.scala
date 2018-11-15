package matcher

import akka.actor.{Actor, Props}
import matcher.DispenserActor.{OperationHasBeenPerformedConfirmation, OrderApproveResult, OrderApproved, OrderDenied}


object ClientStatesActor {

  def props(): Props = Props(classOf[ClientStatesActor])

  case class SetClientState(clientState: ClientState)

  sealed trait UpdateClientState

    case class UpdateClientStateMoney(name: Client, money: Int) extends UpdateClientState

    case class UpdateClientStateStock(name: Client, ticker: Char, quantity: Quantity) extends UpdateClientState

  case class PleaseApproveThisOrder(order: Order)

}


case class ClientStatesActor() extends Actor {

    private val states = scala.collection.mutable.Map[Client, ClientState]()

    private var countBeforePrint = countOfTickers

    import ClientStatesActor._

    def receive:Receive = {

      case SetClientState(c@ClientState(name, _, _)) => states(name) = c

      case UpdateClientStateMoney(name: Client, money: Int) =>
        val currentAccount = states(name)
        states(name) = currentAccount.copy(money = currentAccount.money + money)
        sender() ! OperationHasBeenPerformedConfirmation()

      case UpdateClientStateStock(name, ticker, quantity) =>
        val previousClientState = states(name)
        states(name) = previousClientState.copy(stock = previousClientState.stock.updated(ticker, previousClientState.stock(ticker) + quantity))
        sender() ! OperationHasBeenPerformedConfirmation()

      case CancelAllOrders() =>
        countBeforePrint += -1
        if (countBeforePrint == 0) sender() ! getStateString

      case PleaseApproveThisOrder(o) => sender() ! approveOrDeny(o)

    }

  private def approveOrDeny(o:Order):OrderApproveResult = o match {
    case SellOrder(client, ticker, _ , quantity) =>
      if (states.get(client).flatMap(c => c.stock.get(ticker)).exists(count => count >= quantity)) OrderApproved(o) else OrderDenied()
    case BuyOrder(client, _ , price, quantity) =>
      if (states.get(client).exists(c => c.money >= price * quantity)) OrderApproved(o) else OrderDenied()

  }

  private def getStateString: String =
    (for {
      (client, clientState) <- states


    } yield {
      List(client, clientState.money,
        clientState.stock.getOrElse('A', 0),
        clientState.stock.getOrElse('B', 0),
        clientState.stock.getOrElse('C', 0),
        clientState.stock.getOrElse('D', 0))
        .mkString("\t")
    }).toList.sorted.mkString("\n")

  }
