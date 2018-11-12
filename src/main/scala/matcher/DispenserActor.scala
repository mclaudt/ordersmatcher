package matcher

import akka.actor.{Actor, ActorLogging, ActorRef, Stash}

class DispenserActor(stockActors: Map[Char, ActorRef],clientStateActor:ActorRef) extends Actor with Stash with ActorLogging {


  import context._


  var counter = 0

  def receive: Receive = {



    case io: Order => {
      log.debug(s"Order has been sent for approval ${io}")
      clientStateActor ! PleaseApproveThisOrder(io)
      unstashAll()
      become(waitForApproval)
    }

    case CancelAllOrders() => stockActors.values.foreach(a => a.!(CancelAllOrders())(sender()))

    case msg => stash()
  }
  def waitForApproval: Receive = {
    case OrderApproved(io) ⇒ {
      log.debug(s"Order approved and will be sent ${io}")
      stockActors(io.abcd) ! io
      unstashAll()
      become(waitForN)
    }
    case OrderDenied() ⇒ {
      log.debug(s"Order denied")
      unstashAll()
      become(receive)
    }
    case msg => stash()

  }


  def waitForN: Receive = {
    case WaitForNResponses(i) ⇒ {
      log.debug(s"Will wait for ${i} ticks")
      counter = i
      unstashAll()
      become(counting)
    }
    case msg => stash()

  }

  def counting: Receive = {
    case "tick" ⇒ {
      counter += -1
      log.debug(s"Tick received, ${counter} ticks left")
      if (counter == 0) {
        unstashAll()
        become(receive)
      }
    }
    case msg => stash()

  }


}