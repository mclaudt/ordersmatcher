package matcher

import akka.actor.{Actor, ActorLogging, ActorRef}

import collection.mutable.ArrayBuffer._
import scala.collection.mutable.ArrayBuffer

class StockActor(abcd: Char, clientStateActor: ActorRef) extends Actor with ActorLogging {

  type Counterparts = scala.collection.mutable.Map[Int, scala.collection.mutable.Map[Int, List[Client]]]

  var stateBuyers = scala.collection.mutable.Map[Int, scala.collection.mutable.Map[Int, List[Client]]]()
  var stateCellers = scala.collection.mutable.Map[Int, scala.collection.mutable.Map[Int, List[Client]]]()

  val state = Map(
    'b' -> stateBuyers,
    's' -> stateCellers
  )

  private def serveUpdateStock(u: UpdateStock): Unit = u match {

    case UpdateStockWithNewList(bs, price, quantity, newListOfCounterparts) => state(bs)(price)(quantity) = newListOfCounterparts

    case UpdateStockWithNewClient(bs, price, quantity, newClient) => {
      val oldList = state(bs).getOrElseUpdate(price, scala.collection.mutable.Map[Int, List[Client]](quantity -> List())).getOrElseUpdate(quantity, List())
      state(bs)(price)(quantity) = newClient +: oldList
    }

    case _ => log.error("Unknown UpdateStock operation")


  }


  private def serveInitialOrder(io: Order, sender: ActorRef): Unit = {

    val c = findCounterpart(io)

    if (c.isDefined) log.debug(s"We have a deal: order=${io}, counterpart=${c.get._1}")

    val t = io.getTransactionsAndEffects(c)

    sender ! WaitForNResponses(t._1.length)


    t._2.foreach(u => serveUpdateStock(u))

    t._1.foreach(u => clientStateActor.!(u)(sender))


  }


  def receive: Receive = {

    case io: Order => serveInitialOrder(io, sender())

    case CancelAllOrders() => {
      //Возвращаем деньги покупателям
      ((for {
        (price, map) <- stateBuyers
        (quantity, clients) <- map
        client <- clients
      } yield UpdateClientStateMoney(client, +quantity * price)) ++
        //Возвращаем акции продавцам
        (for {
          (price, map) <- stateCellers
          (quantity, clients) <- map
          client <- clients
        } yield UpdateClientStateStock(client, abcd, +quantity))).foreach(u => clientStateActor ! u)
      clientStateActor.!(CancelAllOrders())(sender())

    }

  }


  private def findCounterpart(io: Order): Option[(Client, List[Client])] = {

    val c: Counterparts = io match {
      case SellOrder(_, _, _, _) => stateBuyers
      case BuyOrder(_, _, _, _) => stateCellers
    }

    for {
      counterpartsForThisPrice <- c.get(io.price)
      counterpartsForThisPriceAndQuantity <- counterpartsForThisPrice.get(io.quantity)
      foundCounterpart <- counterpartsForThisPriceAndQuantity.find(c=> c!=io.client)
    } yield (foundCounterpart, ((ArrayBuffer() ++ counterpartsForThisPriceAndQuantity) - foundCounterpart).toList)
  }



}