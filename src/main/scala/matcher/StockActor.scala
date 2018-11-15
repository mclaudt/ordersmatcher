package matcher

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import matcher.ClientStatesActor.{UpdateClientStateMoney, UpdateClientStateStock}
import matcher.DispenserActor.WaitForNConfirmations

import scala.collection.mutable.ArrayBuffer

object StockActor {

  def props(ticker: Char, clientStateActor: ActorRef): Props = Props(new StockActor(ticker, clientStateActor))

  sealed trait UpdateStock

  case class UpdateStockWithNewList(bs: Char, price: Int, quantity: Int, newListOfClients: List[Client]) extends UpdateStock

  case class UpdateStockWithNewClient(bs: Char, price: Int, quantity: Int, client: Client) extends UpdateStock

  case class ProcessThisOrder(order:Order)

}

class StockActor(ticker: Char, clientStateActor: ActorRef) extends Actor with ActorLogging {

  import StockActor._


  type Counterparts = scala.collection.mutable.Map[Price, scala.collection.mutable.Map[Quantity, List[Client]]]

  private val stateBuyers = scala.collection.mutable.Map[Price, scala.collection.mutable.Map[Quantity, List[Client]]]()
  private val stateSellers = scala.collection.mutable.Map[Price, scala.collection.mutable.Map[Quantity, List[Client]]]()

  private val state = Map(
    'b' -> stateBuyers,
    's' -> stateSellers
  )

  def receive: Receive = {

    case ProcessThisOrder(io) => serveInitialOrder(io, sender())

    case CancelAllOrders() =>
      //Возвращаем деньги покупателям
      ((for {
        (price, map) <- stateBuyers
        (quantity, clients) <- map
        client <- clients
      } yield UpdateClientStateMoney(client, +quantity * price)) ++
        //Возвращаем акции продавцам
        (for {
          (_, map) <- stateSellers
          (quantity, clients) <- map
          client <- clients
        } yield UpdateClientStateStock(client, ticker, +quantity))).foreach(u => clientStateActor ! u)
      clientStateActor.!(CancelAllOrders())(sender())

  }

  private def serveInitialOrder(io: Order, sender: ActorRef): Unit = {

    val c = findCounterpart(io)

    if (c.isDefined) log.debug(s"We have a deal: order=$io, counterpart=${c.get._1}")

    val t = io.getTransactionsAndEffects(c)

    sender ! WaitForNConfirmations(t._1.length)


    t._2.foreach(u => serveUpdateStock(u))

    t._1.foreach(u => clientStateActor.!(u)(sender))


  }

  private def serveUpdateStock(u: UpdateStock): Unit = u match {

    case UpdateStockWithNewList(bs, price, quantity, newListOfCounterparts) => state(bs)(price)(quantity) = newListOfCounterparts

    case UpdateStockWithNewClient(bs, price, quantity, newClient) =>
      val oldList = state(bs).getOrElseUpdate(price, scala.collection.mutable.Map[Quantity, List[Client]](quantity -> List())).getOrElseUpdate(quantity, List())
      state(bs)(price)(quantity) = oldList :+ newClient

    case _ => log.error("Unknown UpdateStock operation")


  }

  private def findCounterpart(io: Order): Option[(Client, List[Client])] = {

    val c: Counterparts = io match {
      case SellOrder(_, _, _, _) => stateBuyers
      case BuyOrder(_, _, _, _) => stateSellers
    }

    for {
      counterpartsForThisPrice <- c.get(io.price)
      counterpartsForThisPriceAndQuantity <- counterpartsForThisPrice.get(io.quantity)
      foundCounterpart <- counterpartsForThisPriceAndQuantity.find(c=> c!=io.client)
    } yield (foundCounterpart, ((ArrayBuffer() ++ counterpartsForThisPriceAndQuantity) - foundCounterpart).toList)
  }



}
