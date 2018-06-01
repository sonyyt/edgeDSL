package compiler

import compiler._
import parser._

class ControlFlowGraph(service: MS_AST.MobileServiceObj) {
  var mobileService = service
  var cfg = new MobileServiceGraph
  var globalParams: Array[String] = Array[String]();
  if (!mobileService.details.getInputParameters.equals("")) {
    //println(mobileService.details.getInputParameters)
    globalParams = mobileService.details.getInputParameters.split("\\|");
  }

  def initVertexes(): Unit = {
    for (microservice <- service.details.microservices.lis) {
      val msID = microservice.identity
      var requiredParam = List[String]()
      var paramsWithValue: Map[String, String] = Map()

      var deviceSelectionString: String = null

      val vertexID = cfg.addVertex(MicroServiceTask, msID) // add nodes as graph node.
      var deviceSelectionRuleString = ""

      // parsing device selection rules, and input parameters.
      for (detail <- microservice.msDetails.details) {
        if (detail.MsType == "deviceSelection") {
          val deviceSelectionRules: MS_AST.DeviceDetail = detail.asInstanceOf[MS_AST.DeviceDetail]
          deviceSelectionString = deviceSelectionRules.parseRules
          //here, we need to store the device selection SQL;
        }
        if (detail.MsType == "microservice input") {
          val msInput: MS_AST.InputDetail = detail.asInstanceOf[MS_AST.InputDetail]
          var param = msInput.ParameterName
          val paramValue = msInput.ParameterValue
          if (!paramValue.equals("_")) {
            //do we need to divide the input of the ms, input from other ms, and ..?
            paramsWithValue += (param -> paramValue)
          } else {
            var flag = false
            for (globalParam <- globalParams) {
              if (param.equals(globalParam)) {
                flag = true
                //println(param)
                paramsWithValue += (param -> "__G__")
              }
            }
            if (!flag) {
              requiredParam ::= param
            }
          }
        }
      }
      cfg.getVertex(vertexID).addDetails(requiredParam, paramsWithValue, deviceSelectionString)
    }
  }

  def initEdgesbyControlFlow = {
    for (microservice <- service.details.microservices.lis) {
      val msID = microservice.identity
      for (detail <- microservice.msDetails.details) {
        if (detail.MsType == "On Condition") {
          //do some correctness check here.
          val msCondition: MS_AST.OnConditionDetail = detail.asInstanceOf[MS_AST.OnConditionDetail]
          val condition = msCondition.condition.getConditionString()
          val outputs = msCondition.ops
          val redir = msCondition.redir
          var returnParams = Map[String, String]()
          var nextNode = cfg.FailureEnd //if any value in return, we decide it is a success return; or, it is a failure return;
          if (redir.Type.eq("return")) {
            for (output <- outputs.lis) {
              if (output.outputType.eq("toMS")) {
                throw new Exception("redirected to MS!")
              }
              nextNode = cfg.SuccessEnd
              returnParams += (output.executionResult -> output.outputAs)
            }
            cfg.addEdge(cfg.getVertex(msID), nextNode, condition, returnParams)
          }
          if (redir.Type.eq("toMS")) {
            for (output <- outputs.lis) {
              if (output.outputType.eq("toReturn")) {
                throw new Exception("redirected to return!")
              }
              returnParams += (output.executionResult -> output.outputAs)
            }

            var redirMS: MS_AST.RedirToMS = redir.asInstanceOf[MS_AST.RedirToMS]
            cfg.addEdge(cfg.getVertex(msID), cfg.getVertex(redirMS.targetMSID), condition, returnParams)
          }
          if (redir.Type.eq("null")) {
            // need to change a name for null. waiting for redirect?
            for (output <- outputs.lis) {
              if (output.outputType.eq("toReturn")) {
                throw new Exception("redirected to return!")
              }
              returnParams += (output.executionResult -> output.outputAs)
            }
            cfg.addEdge(cfg.getVertex(msID), cfg.specialVertex, condition, returnParams)
            //now the edge links to a null node
          }
        }
      }
    }
  }

  /* for each single node:
   *   1.does it require input? no: continue on next node.
   *   2.does it's input edge provide all required inputs? yes: continue; no: goto 3;
   *   3.is there only one node that provide the required input? yes: add that node into the graph; no: goto 5;
   *   4.add parallel node; with data result requirements. all possible input nodes should be added.
   *   5. loop
  */


  def initEdgesbyDataFlow = {
    var i = 0;
    var inputEdges = cfg.traceIncomeEdges(cfg.specialVertex)
    var vertexSet = scala.collection.mutable.Set[Vertex]()
    for (vertex <- cfg.CFG.vertices) {
      if (vertex.vType.equals(MicroServiceTask) && !inputEdges.exists(_.sourceVertex.equals(vertex))) {
        vertexSet.add(vertex)
      }
    }
    while (vertexSet.nonEmpty) {
      val n = util.Random.nextInt(vertexSet.size)
      var vertex = vertexSet.iterator.drop(n).next
      vertexSet.remove(vertex)
      var inputRequests = vertex.requiredInput
      if (inputRequests.nonEmpty) {
        var inputEgesOfVertex = cfg.traceIncomeEdges(vertex)
        // for each edge that may lead to this execution
        for (edge <- inputEgesOfVertex) {
          var missingParams = scala.collection.mutable.Set[String]()
          var requiredEdges = scala.collection.mutable.Set[Edge]()
          for (request <- inputRequests) {
            if (edge.param == null || !edge.param.exists(_._2 == request)) {
              missingParams.add(request)
            }
          }
          if (missingParams.nonEmpty) {
            //if flag = false, input is required for this incoming edge.
            // any problem here? can it require the execution of multiple active branches?: we assume for now;
            //if one and only one inputEdges is required?
            for (missingParam <- missingParams) {
              var paramProvidingEdges = inputEdges.filter(_.param.exists(_._2 == missingParam))
              println(paramProvidingEdges + "found")
              if (paramProvidingEdges.isEmpty) {
                throw new Exception("Cannot Find Input Param to Microservice!" + vertex + " on edge" + edge)
              }
              requiredEdges = requiredEdges ++ paramProvidingEdges // merge two sets;
            }
            // add to vertexSet
            for (requiredEdge <- requiredEdges) {
              vertexSet.add(requiredEdge.sourceVertex)
            }
            if (requiredEdges.size == 1) { // no need to parallel, add to vertexSet;
              //println("data input added for"+edge.targetVertex)
              //println("node to add"+requiredEdges.head.sourceVertex)
              cfg.removeEdge(edge)
              cfg.addEdge(edge.sourceVertex, requiredEdges.head.sourceVertex, edge.condition, edge.param)
              cfg.addEdge(requiredEdges.head.sourceVertex, edge.targetVertex, requiredEdges.head.condition, requiredEdges.head.param)
            } else { // add parallel execution node;
              //println("parallel init")
              //println("data input added for"+edge.targetVertex)
              //println("node to add"+requiredEdges)
              var parallelEnd = cfg.getVertex(cfg.addVertex(ParallelEnd))
              var parallelStart = cfg.getVertex(cfg.addVertex(ParallelStart, parallelEnd.vertexID.toString()))
              parallelEnd.requiredInput = missingParams.toList
              cfg.removeEdge(edge)
              cfg.addEdge(edge.sourceVertex, parallelStart, edge.condition, edge.param)
              cfg.addEdge(parallelEnd, edge.targetVertex, null, null)
              for (requiredEdge <- requiredEdges) {
                cfg.addEdge(requiredEdge.sourceVertex, parallelEnd, requiredEdge.condition, requiredEdge.param)
                cfg.addEdge(parallelStart, requiredEdge.sourceVertex, null, null)
              }
            }
          }
        }
      }
    }
  }

  def getSLA: Double = {
    var successEdges = cfg.traceIncomeEdges(cfg.SuccessEnd)
    var SLAE: Double = 0 //ServiceLevelAgreementEstimation
    if (successEdges.size != 0) {
      for (edge <- successEdges) {
        var edgeSLA = calculateSLA(edge)
        //println("edgeSLA:"+edge.sourceVertex.msID+edgeSLA)
        SLAE += edgeSLA
      }
    }
    SLAE
  }

  def calculateSLA(edge: Edge): Double = {
    var currentEdge = edge
    if (currentEdge.sourceVertex.vType.equals(StartExecution) || currentEdge.sourceVertex.vType.equals(ParallelStart)) {
      //println("StartExecution or ParallelStart")
      return 1;
    }
    if (currentEdge.sourceVertex.vType.equals(MicroServiceTask)) {
      var fatherEdge = cfg.traceIncomeEdges(currentEdge.sourceVertex).head
      // for each execution edge, it should have only one father edge;
      //println("sourceVertex"+currentEdge.sourceVertex.msID+cfg.calculateSuccessRate(currentEdge))
      return calculateSLA(fatherEdge) * cfg.calculateSuccessRate(currentEdge)
    }
    //1. calculate possibility for each parameters;
    //2. all possibility times each other.
    if (currentEdge.sourceVertex.vType.equals(ParallelEnd)) {
      var SLA_end: Double = 1
      var inputEdges = cfg.traceIncomeEdges(currentEdge.sourceVertex)
      var requiredParams = currentEdge.sourceVertex.requiredInput
      for (missingParam <- requiredParams) {
        //println("\tparam"+missingParam)
        var SLA_param: Double = 0
        var paramProvidingEdges = inputEdges.filter(!_.param.find(_._2 == missingParam).isEmpty)
        for (paramProvidingEdge <- paramProvidingEdges) {
          SLA_param = SLA_param + calculateSLA(paramProvidingEdge)
        }
        SLA_end = SLA_end * SLA_param
      }
      //println("\tsla-paral-end"+SLA_end)
      return SLA_end
    }
    return -1;
  }

  def findEntryVertex = {
    var entryVertex = scala.collection.mutable.Set[Vertex]()
    var inputEdges = cfg.traceIncomeEdges(cfg.specialVertex)
    for (microservice <- service.details.microservices.lis) {
      //println(RequiredParams)
      var edges = cfg.traceIncomeEdges(cfg.getVertex(microservice.identity))
      if (edges.size == 0) {
        if (inputEdges.filter(_.sourceVertex.msID == microservice.identity).size == 0) {
          entryVertex.add(cfg.getVertex(microservice.identity))
          //it isn't an input node
        }
      }
    }
    if (entryVertex.size != 1) {
      throw new Exception("Execution Graph Fail")
    } else {
      //println("entry node added")
      cfg.addEdge(cfg.startVertex, entryVertex.head, null, null)
    }
  }

  def removeSpecialNode = {
    var inputEdges = cfg.traceIncomeEdges(cfg.specialVertex)
    for (inputEdge <- inputEdges) {
      cfg.removeEdge(inputEdge)
    }
    cfg.removeVertex(cfg.specialVertex);

  }

  def generateExecutionGraph = {
    this.initVertexes;
    this.initEdgesbyControlFlow
    this.initEdgesbyDataFlow
    this.findEntryVertex
    this.removeSpecialNode
  }
}