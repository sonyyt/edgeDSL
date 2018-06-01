package compiler

import scala.io.Source
import scala.util.parsing
import compiler._
import java.nio.charset.StandardCharsets
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json._

class graphToJson(service: ControlFlowGraph) {
  var cfg = service.cfg

  def toJson: String = {
    implicit val vertexWrites = new Writes[Vertex] {
      def writes(vert: Vertex) = Json.obj(
        "microServiceTask" -> vert.msID,
        "vertexID" -> vert.vertexID,
        "nodeType" -> vert.vType.vType,
        "device" -> vert.deviceSelection,
        "requiredInput" -> vert.requiredInputString,
        "params" -> vert.paramsString
      )
    }

    implicit val edgeWrites = new Writes[Edge] {
      def writes(edge: Edge) = Json.obj(
        "source" -> edge.sourceVertex.vertexID,
        "target" -> edge.targetVertex.vertexID,
        "condition" -> edge.condition,
        "param" -> edge.paramsString
      )
    }

    implicit val cfgWrites = new Writes[MobileServiceGraph] {
      def writes(cfg: MobileServiceGraph) = Json.obj(
        "edges" -> cfg.CFG.edges,
        "nodes" -> cfg.CFG.vertices
      )
    }

    implicit val serviceWrites = new Writes[ControlFlowGraph] {
      def writes(service: ControlFlowGraph) = Json.obj(
        "serviceID" -> service.mobileService.ServiceID.ServiceID,
        "requiredParam" -> service.mobileService.details.getInputParameters,
        "controlFlowGraph" -> service.cfg
      )
    }

    val cfgJson = Json.toJson(service)
    println(cfgJson.toString)
    cfgJson.toString
  }

  def parseVertex() {
    for (vertex <- cfg.CFG.vertices) {
      var paramsString = ""
      var requiredInputString = ""
      if (vertex.Params != null && vertex.Params.nonEmpty) {
        for ((k, v) <- vertex.Params) {
          paramsString = paramsString + "|" + k + ":" + v
        }
      }
      if (vertex.requiredInput != null && vertex.requiredInput.nonEmpty) {
        for (requiredInput <- vertex.requiredInput) {
          requiredInputString = requiredInputString + "|" + requiredInput
        }
      }
      vertex.paramsString = paramsString.trim
      vertex.requiredInputString = requiredInputString.trim
    }
  }

  def parseEdge() {
    for (edge <- cfg.CFG.edges) {
      var paramsString = "";
      if (edge.param != null && edge.param.nonEmpty) {
        for ((k, v) <- edge.param) {
          paramsString = paramsString + "|" + k + ":" + v
        }
      }
      edge.paramsString = paramsString.trim
    }
  }

  def save(fileName: String) {
    import java.nio.file.{Paths, Files}
    this.parseVertex()
    this.parseEdge()
    Files.write(Paths.get(fileName), this.toJson.getBytes(StandardCharsets.UTF_8))
  }
}