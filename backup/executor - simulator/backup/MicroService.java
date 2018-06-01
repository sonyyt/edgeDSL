package msExecution;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class MicroService{
	private String serviceID;
	private ArrayList<String> requiredParam;
	private ControlFlowGraph controlFlowGraph;
  
	public String getServiceID() { return this.serviceID; }
	public void setServiceID(String serviceID) { this.serviceID = serviceID; }
	
	public ArrayList<String> getRequiredParam() { return this.requiredParam; }
	public void setRequiredParam(ArrayList<String> requiredParam) { this.requiredParam = requiredParam; }
	
	public ControlFlowGraph getControlFlowGraph() { return this.controlFlowGraph; }
	public void setControlFlowGraph(ControlFlowGraph controlFlowGraph) { this.controlFlowGraph = controlFlowGraph; }
  
  // inner class ControlFlowGraph
	public class ControlFlowGraph{
		private List<Edge> edges;
		//private ArrayList<Node> nodes;
		
		public List<Edge> getEdges() { return this.edges; }
		public void setEdges(List<Edge> edges) { this.edges = edges; }
//		
//		public ArrayList<Node> getNodes() { return this.nodes; }
//		public void setNodes(ArrayList<Node> nodes) { this.nodes = nodes; }
	}

	/*
	 * Inner Class Edge: 
	 * source: vertexID of source node;
	 * target: vertexID of target node; 
	 * condition: String: format~MS_AST.scala
	 * param: |k+v|k+v
	 */

	public class Edge{
		private int source;
		private int target;
		//private String condition;
		//private String param;
			  
		public int getSource() { return this.source; }
		public void setSource(int source) { this.source = source; }
			
		public int getTarget() { return this.target; }
		public void setTarget(int target) { this.target = target; }
			
//		public String getCondition() { return this.condition; }
//		public void setCondition(String condition) { this.condition = condition; }	
//		public String getParam() { return this.param; }
//		public void setParam(String param) { this.param = param; }
	}
  /*
   * inner class Node:
   * vertexID: unique ID of each node;
   * microServiceTask: MicroService Identification;
   * nodeType: String (should be enum: microService,startExecution,endExecution,startParallel,endParallel,special)
   * reqwuiredInput:|String|String
   * params: |k:v|k:v
   */
	public class Node{
		private int vertexID;
		private String microServiceTask;
		private String nodeType;
		private String device;
		private String requiredInput;
		private String params;
	
		public String getMicroServiceTask() { return this.microServiceTask; }
		public void setMicroServiceTask(String microServiceTask) { this.microServiceTask = microServiceTask; }
	
	
		public int getVertexID() { return this.vertexID; }
		public void setVertexID(int vertexID) { this.vertexID = vertexID; }
	
	
		public String getNodeType() { return this.nodeType; }
		public void setNodeType(String nodeType) { this.nodeType = nodeType; }
	
	    
		public String getDevice() { return this.device; }
		public void setDevice(String device) { this.device = device; }
	
	    
		public String getRequiredInput() { return this.requiredInput; }
		public void setRequiredInput(String requiredInput) { this.requiredInput = requiredInput; }
	 
	
		public String getParams() { return this.params; }
		public void setParams(String params) { this.params = params; }
	}

}









