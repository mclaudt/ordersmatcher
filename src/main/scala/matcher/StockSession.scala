package matcher

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.Await
import scala.concurrent.duration._



case class StockSession(initialState:Iterator[ClientState], orders:Iterator[Order])(implicit timeout: Timeout = Timeout(5 minutes)) {


  private val system = ActorSystem("stockSessionActorSystem")

  private val clientStateActor = system.actorOf(Props[ClientStatesActor], name = "clientStateActor")

  private val stockActors: Map[Char, ActorRef] =

    List('A', 'B', 'C', 'D').map(c => c -> system.actorOf(Props(classOf[StockActor], c, clientStateActor), name = s"stockActor${c}")).toMap

  private val dispenserActor = system.actorOf(Props(classOf[DispenserActor], stockActors,clientStateActor), name = "dispenserActor")

  initialState.foreach(s=> clientStateActor ! s)
  orders.foreach(o=>dispenserActor ! o)






  def result: String = {val res = Await.result(dispenserActor ? CancelAllOrders(), timeout.duration).asInstanceOf[String]
    system.terminate
    res
  }



}
