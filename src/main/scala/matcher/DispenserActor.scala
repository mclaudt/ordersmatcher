package matcher

import akka.actor.{Actor, ActorLogging, ActorRef, Stash}

class DispenserActor(stockActors: Map[Char, ActorRef],clientStateActor:ActorRef) extends Actor with Stash with ActorLogging {

  import context.become

  var counter = 0

  def receive: Receive = {

    case io: Order =>
      log.debug(s"Order has been sent for approval $io")
      clientStateActor ! PleaseApproveThisOrder(io)
      unstashAll()
      become(waitForApproval)

    case CancelAllOrders() => stockActors.values.foreach(a => a.!(CancelAllOrders())(sender()))

    case _ => stash()
  }

  def waitForApproval: Receive = {
    case OrderApproved(io) =>
      log.debug(s"Order approved and will be sent $io")
      stockActors(io.ticker) ! io
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