import java.io.{File, FileWriter}
import java.nio.file.Paths

package object matcher {

  type Client = String

  type Price = Int

  type Quantity = Int

  // Universal messages

  case class CancelAllOrders()

  //Utils

  private def writeToFile(file: File, str: String): Unit = {
    val writer = new FileWriter(file)
    try {
      writer.append(str).append("\n")
    }
    finally {
      writer.close()
    }

  }

  def writeToFileByName(fileName:String = "result.txt",result:String): Unit = writeToFile(Paths.get(fileName).toFile,result)

}
