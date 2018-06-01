package parser
import MobileServiceDSL._
import scala.io.Source
import compiler._

object testDSL {
  val filename = System.getProperty("user.dir")+"/src/main/scala/resources/temperature3.serv" 
  val str = Source.fromFile(filename).getLines.mkString

  def main(args: Array[String]){
    //println(str)
    MobileService(new lexical.Scanner(str)) match {
      case Success(service, _) => {
        println("Processing Mobile Service"+service.ServiceID.ServiceID)
        val serviceGraph = new ControlFlowGraph(service)
        serviceGraph.generateExecutionGraph
        //println("SLA:"+serviceGraph.getSLA)
        val toJson = new graphToJson(serviceGraph)
        //println(toJson)
        toJson.save("nearby_temperature3")
     }
     case Failure(msg, _) => println("Failure:"+msg)
     case Error(msg, _) => println("Error"+msg)
    }
    
  }
}