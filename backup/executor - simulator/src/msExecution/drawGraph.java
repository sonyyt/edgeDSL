package msExecution;

import info.leadinglight.jdot.Edge;
import info.leadinglight.jdot.Graph;
import info.leadinglight.jdot.Node;
import info.leadinglight.jdot.enums.Color;
import info.leadinglight.jdot.enums.Color.X11;
import info.leadinglight.jdot.enums.Shape;
import msExecution.MicroService.ControlFlowGraph;

import java.nio.file.Paths;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;


public class drawGraph {
	Graph g;
	ControlFlowGraph cfg;
	
	public drawGraph(ControlFlowGraph c){
		g = new Graph("structs");
		cfg = c;
		Graph.setDefaultCmd("C://Program Files (x86)/Graphviz2.38/bin/dot");
	}
	
	
	public void parseExecutionGraph(){
		msExecution.MicroService.ControlFlowGraph.Edge[] edges = cfg.edges;
		msExecution.MicroService.ControlFlowGraph.Node[] nodes = cfg.nodes;
		for(int nodeCounter=0;nodeCounter<nodes.length;nodeCounter++){
			msExecution.MicroService.ControlFlowGraph.Node node = nodes[nodeCounter];
			g.addNodes(new Node(String.valueOf(node.vertexID)).setShape(Shape.record).setLabel(node.microServiceTask+","+node.nodeType+","+node.requiredInput));
		}
		for(int edgeCounter=0;edgeCounter<edges.length;edgeCounter++){
			msExecution.MicroService.ControlFlowGraph.Edge edge = edges[edgeCounter];
			X11 color = Color.X11.black;
			if(edge.passed==true){
				color = Color.X11.red;
			}
			g.addEdges(new Edge().setArrowHead("").addNode(String.valueOf(edge.source)).addNode(String.valueOf(edge.target)).setLabel(edge.condition+","+edge.param).setColor(color));
		}
		this.saveToFile();
	}
	
	public void saveToFile(){
	    try {
			Files.write(Paths.get("dot.svg"), g.dot2svg().getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
