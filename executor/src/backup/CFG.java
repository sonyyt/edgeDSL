package backup;

import java.util.ArrayList;

import backup.CFG.Node;
import msExecution.MicroService;

public class CFG {
	public static final String NodeType_MS = "microService";
	public static final String NodeType_START = "startExecution";
	public static final String NodeType_END = "endExecution";
	public static final String NodeType_PARA_START = "startParallel";
	public static final String NodeType_PARA_END = "endParallel";
	public static final String NodeType_ALTER_START = "startAlternative";
	public static final String NodeType_ALTER_END = "endAlternative";

	


	

	
	public Node nextNode(Node currentNode, MicroService ms){
		return currentNode;
	}
	
	public Node findPeerNode(Node currentNode){
		return currentNode;
	}
	
	public ArrayList<Node> nextParallelNodes(Node currentNode){
		ArrayList<Node> nodes= new ArrayList<Node>();
		nodes.add(currentNode);
		return nodes;
	}
	
	public class Node{
		public String nodeType;
		public int microServiceTaskID;
		public int nodeID;
	}
	
	public class Edge{
		public int source;
		public int target;
		public String condition;
	}
}
