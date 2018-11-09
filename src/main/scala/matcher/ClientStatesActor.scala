package matcher

import akka.actor.Actor

class ClientStatesActor extends Actor {

    private var states = scala.collection.mutable.Map[Client, ClientState]()

    private var countBeforePrint = 4

    def receive:Receive = {

      case c@ClientState(name, _, _) => {
        states(name) = c

      }
      case UpdateClientStateMoney(name: Client, money: Int) => {
        val currentAccount = states(name)
        states(name) = currentAccount.copy(money = currentAccount.money + money)
        sender() ! "tick"
      }


      case UpdateClientStateStock(name, abcd, quantity) => {
        states(name).stock.getOrElseUpdate(abcd, 0)
        states(name).stock(abcd) = states(name).stock(abcd) + quantity
        sender() ! "tick"
      }

      case CancelAllOrders() => {
        countBeforePrint += -1
        if (countBeforePrint == 0) sender() ! getStateString
      }
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

