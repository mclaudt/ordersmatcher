package matcher

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import matcher.ClientStatesActor.SetClientState
import matcher.DispenserActor.IncomingOrder

import scala.concurrent.Await
import scala.concurrent.duration._



case class StockSession(initialState:Iterator[ClientState], orders:Iterator[Order])(implicit timeout: Timeout = Timeout(5 minutes)) {


  private val system = ActorSystem("stockSessionActorSystem")

  private val clientStateActor = system.actorOf(ClientStatesActor.props(), name = "clientStateActor")

  private val stockActors: Map[Char, ActorRef] =

    List('A', 'B', 'C', 'D').map(ticker => ticker -> system.actorOf(StockActor.props(ticker, clientStateActor), name = s"stockActor$ticker")).toMap

  private val dispenserActor = system.actorOf(DispenserActor.props(stockActors,clientStateActor), name = "dispenserActor")

  initialState.foreach(s=> clientStateActor ! SetClientState(s))
  orders.foreach(o=>dispenserActor ! IncomingOrder(o))

  def result: String = {val res = Await.result(dispenserActor ? CancelAllOrders(), timeout.duration).asInstanceOf[String]
    system.terminate
    res
  }


}
