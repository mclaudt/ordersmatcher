package matcher

import scala.io.Source
import scala.util.{Failure, Success, Try}
import org.slf4j.Logger
import org.slf4j.LoggerFactory


object IOUtils {

  private val logger = LoggerFactory.getLogger(IOUtils.getClass)

  def getInitialStateFromResources(fileName: String): Iterator[ClientState] = {
    val unfilteredIterator = Source.fromResource(fileName).getLines.map(l => Try {
      l.split("\t").map(s => s.trim) match {
        case Array(name, money, a, b, c, d) =>
          ClientState(
            name,
            money.toInt,
            scala.collection.mutable.Map(
              'A' -> a.toInt,
              'B' -> b.toInt,
              'C' -> c.toInt,
              'D' -> d.toInt
            )
          )
        case _ => throw InvalidStringInInputFileException(l)
      }
    }
    )

    logAndFilterIterator(unfilteredIterator, fileName)
  }

  def getOrdersFromFileInResources(fileName: String): Iterator[Order] = {

    val unfilteredIterator = Source.fromResource(fileName).getLines.map(l => Try {
      l.split("\t").map(s => s.trim) match {
        case Array(client, bs, ticker, price, quantity) =>

          bs.head match {
            case 'b' => SellOrder(client, ticker.head, price.toInt, quantity.toInt)
            case 's' => BuyOrder(client, ticker.head, price.toInt, quantity.toInt)
            case _ => throw InvalidStringInInputFileException(s"'$bs' in line $l")
          }

        case _ => throw InvalidStringInInputFileException(l)

      }
    }
    )

    logAndFilterIterator(unfilteredIterator, fileName)

  }

  private def logAndFilterIterator[V](i: Iterator[Try[V]], fileName: String): Iterator[V] =
    i.map(t => t.recoverWith {
      case e: Throwable =>
        logger.error(s"Error while parsing input file $fileName", e)
        Failure(e)
    }).collect { case Success(x) => x }

  case class InvalidStringInInputFileException(desc: String) extends Exception(desc)

}
