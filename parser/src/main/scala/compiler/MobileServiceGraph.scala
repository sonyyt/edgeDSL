package compiler
import compiler._

trait VertexType{
  def vType:String
}
case object MicroServiceTask extends VertexType { val vType = "microService" }
case object StartExecution extends VertexType { val vType = "startExecution" }
case object SuccessEndExecution extends VertexType { val vType = "SuccessEndExecution" }
case object FailureEndExecution extends VertexType { val vType = "FailureEndExecution" }
case object ParallelStart extends VertexType { val vType = "startParallel" }
case object ParallelEnd extends VertexType { val vType = "endParallel" }
case object Special extends VertexType { val vType = "special" }

case class Vertex(vType:VertexType, msID: String, vertexID: Int,  var requiredInput: List[String], var Params: Map[String, String], var deviceSelection:String){
  var paramsString = ""
  var requiredInputString = "" 
  override def toString():String={
    "type"+vType+";msID"+msID + ";vertexID" + vertexID
  }
  
  def addDetails(requiredInput: List[String], Params: Map[String, String], deviceSelection:String){
    this.requiredInput = requiredInput
    this.Params = Params
    this.deviceSelection = deviceSelection
  }
}
case class Edge(sourceVertex:Vertex, targetVertex: Vertex, condition:String, param:Map[String,String]) extends Topology.Edge[Vertex]{
  
  var paramsString = ""
}


class MobileServiceGraph {
  var CFG = Topology.empty[Vertex, Edge];
  var startVertex = getVertex(addVertex(StartExecution,null))
  var SuccessEnd = getVertex(addVertex(SuccessEndExecution, null))
  var FailureEnd = getVertex(addVertex(FailureEndExecution, null))
  var specialVertex = getVertex(addVertex(Special, null)) // a special node, where all data input node are linked to this node; 

  
  def addVertex(vt:VertexType,msID:String):Int={
    var existingNodeNumber = CFG.vertices.size;
    var vertexID = existingNodeNumber+1;
    CFG = CFG.addVertex(Vertex(vt,msID,vertexID,null,null,null))
    return vertexID;
  }
  
  def removeVertex(v:Vertex)={
     CFG = CFG.removeVertex(v)
  }
  
  def addVertex(vt:VertexType):Int={
    if(vt.vType=="microService"){
      throw new Exception("interface not for insert MS node")
    }
    var existingNodeNumber = CFG.vertices.size;
    var vertexID = existingNodeNumber+1;
    CFG = CFG.addVertex(Vertex(vt,null,vertexID,null,null,null))
    return vertexID;
  }
  
  def addEdge(sourceVertex:Vertex, targetVertex:Vertex, condition:String, param:Map[String,String]){
    val util.Success((cfg, m0)) = CFG.addEdge(Edge(sourceVertex, targetVertex, condition, param))
    CFG = cfg;
  }
    
  def removeEdge(edge:Edge){
     CFG = CFG.removeEdge(edge)
  }
  
  def getVertex(msID:String):Vertex={
    CFG.vertices.filter(_.msID==msID)(0);
  }
  
  def getVertex(vertexID:Int):Vertex={
    CFG.vertices.filter(_.vertexID==vertexID)(0);
  }
  
  def getVertexIDbyMSID(msID:String):Int={
    var sourceVertex = CFG.vertices.filter(_.msID==msID)(0);
    return sourceVertex.vertexID
  }
  
  def traceIncomeEdges(vertex:Vertex):Set[Edge]={
    CFG.edges.filter(_.targetVertex.equals(vertex))
  }
  
  def calculateSuccessRate(edge:Edge):Double={
    var successfulRate = 0.5
    if(edge.condition == null){
      return 1
    }
    if(edge.condition.charAt(0)=='1'){
      //execution success
      return successfulRate
    }
    if(edge.condition.charAt(0)=='2'){
      return 1-successfulRate
    }
    return 0
  }
  
  
  def debugPrint{
    println(this.CFG.edges);
    println(this.CFG.vertices);
  }
  
  def checkGraph():Boolean={
    CFG.vertices
    CFG.edges
    //if there is a path to the success end node?
    //if there is any loop in the graph?
    //is there any path that cannot go into?
    //
    return true
  }
}