package matcher

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Stash}
import matcher.ClientStatesActor.PleaseApproveThisOrder
import matcher.StockActor.ProcessThisOrder

class DispenserActor(stockActors: Map[Char, ActorRef],clientStateActor:ActorRef) extends Actor with Stash with ActorLogging {

  import DispenserActor._

  import context.become

  var counter = 0

  def receive: Receive = {

    case IncomingOrder(o) =>
      log.debug(s"Order has been sent for approval $o")
      clientStateActor ! PleaseApproveThisOrder(o)
      unstashAll()
      become(waitForApproval)

    case CancelAllOrders() => stockActors.values.foreach(a => a.!(CancelAllOrders())(sender()))

    case _ => stash()
  }

  def waitForApproval: Receive = {
    case OrderApproved(io) =>
      log.debug(s"Order approved and will be sent $io")
      stockActors(io.ticker) ! ProcessThisOrder(io)
      unstashAll()
      become(waitForN)
    case OrderDenied() =>
      log.debug(s"Order denied")
      unstashAll()
      become(receive)
    case _ => stash()

  }


  def waitForN: Receive = {
    case WaitForNConfirmations(i) =>
      log.debug(s"Will wait for $i confirmations")
      counter = i
      unstashAll()
      become(counting)
    case _ => stash()

  }

  def counting: Receive = {
    case OperationHasBeenPerformedConfirmation() =>
      counter += -1
      log.debug(s"Confirmation received, $counter confirmations left")
      if (counter == 0) {
        unstashAll()
        become(receive)
      }
    case _ => stash()

  }


}

object DispenserActor{

  def props(stockActors: Map[Char, ActorRef],clientStateActor:ActorRef): Props = Props(new DispenserActor(stockActors,clientStateActor))

  case class IncomingOrder(order:Order)

  sealed trait OrderApproveResult

    case class OrderApproved(order: Order) extends OrderApproveResult

    case class OrderDenied() extends OrderApproveResult

  case class OperationHasBeenPerformedConfirmation()

  case class WaitForNConfirmations(n: Int)

}
