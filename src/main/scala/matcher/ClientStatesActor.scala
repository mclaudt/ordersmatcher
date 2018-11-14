package matcher

import akka.actor.Actor

class ClientStatesActor extends Actor {

    private val states = scala.collection.mutable.Map[Client, ClientState]()

    private var countBeforePrint = 4

    def receive:Receive = {

      case c@ClientState(name, _, _) => states(name) = c

      case UpdateClientStateMoney(name: Client, money: Int) =>
        val currentAccount = states(name)
        states(name) = currentAccount.copy(money = currentAccount.money + money)
        sender() ! OperationHasBeenPerformedConfirmation()

      case UpdateClientStateStock(name, ticker, quantity) =>
        states(name).stock.getOrElseUpdate(ticker, 0)
        states(name).stock(ticker) = states(name).stock(ticker) + quantity
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

