package matcher

import akka.actor.{Actor, ActorLogging, ActorRef}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class StockActor(ticker: Char, clientStateActor: ActorRef) extends Actor with ActorLogging {

  type Counterparts = scala.collection.mutable.Map[Int, scala.collection.mutable.Map[Int, List[Client]]]

  val stateBuyers: mutable.Map[Int, mutable.Map[Int, List[Client]]] = scala.collection.mutable.Map[Int, scala.collection.mutable.Map[Int, List[Client]]]()
  val stateSellers: mutable.Map[Int, mutable.Map[Int, List[Client]]] = scala.collection.mutable.Map[Int, scala.collection.mutable.Map[Int, List[Client]]]()

  val state = Map(
    'b' -> stateBuyers,
    's' -> stateSellers
  )

  private def serveUpdateStock(u: UpdateStock): Unit = u match {

    case UpdateStockWithNewList(bs, price, quantity, newListOfCounterparts) => state(bs)(price)(quantity) = newListOfCounterparts

    case UpdateStockWithNewClient(bs, price, quantity, newClient) =>
      val oldList = state(bs).getOrElseUpdate(price, scala.collection.mutable.Map[Int, List[Client]](quantity -> List())).getOrElseUpdate(quantity, List())
      state(bs)(price)(quantity) = oldList :+ newClient

    case _ => log.error("Unknown UpdateStock operation")


  }


  private def serveInitialOrder(io: Order, sender: ActorRef): Unit = {

    val c = findCounterpart(io)

    if (c.isDefined) log.debug(s"We have a deal: order=$io, counterpart=${c.get._1}")

    val t = io.getTransactionsAndEffects(c)

    sender ! WaitForNConfirmations(t._1.length)


    t._2.foreach(u => serveUpdateStock(u))

    t._1.foreach(u => clientStateActor.!(u)(sender))


  }


  def receive: Receive = {

    case io: Order => serveInitialOrder(io, sender())

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