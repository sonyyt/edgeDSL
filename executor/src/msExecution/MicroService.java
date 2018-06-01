package msExecution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public final class MicroService {
    public final String serviceID;
    public List<String> requiredGlobalInput;
    public static Map<String, String> globalParams;
    public final ControlFlowGraph controlFlowGraph;

    @JsonCreator
    public MicroService(@JsonProperty("serviceID") String serviceID, @JsonProperty("requiredParam") String requiredParams, @JsonProperty("controlFlowGraph") ControlFlowGraph controlFlowGraph){
        this.serviceID = serviceID;
        if(requiredParams.trim().equals("")){
        	 this.requiredGlobalInput = new ArrayList<String>();
        }else{
        	this.requiredGlobalInput = new ArrayList(Arrays.asList(requiredParams.trim().split("\\|")));
        }
        this.globalParams = new HashMap<String, String>();
        this.controlFlowGraph = controlFlowGraph;
    }

    public boolean addGlobalInput(Map<String, String> input){
    	for(Map.Entry<String, String> inputParam : input.entrySet()){
    		if(requiredGlobalInput.contains(inputParam.getKey())){
    			//System.out.println(requiredGlobalInput.size() + "key" + inputParam.getKey());
    			requiredGlobalInput.remove(requiredGlobalInput.indexOf(inputParam.getKey()));
    			globalParams.put(inputParam.getKey(), inputParam.getValue());
    		}
    	}
		if(!requiredGlobalInput.isEmpty()){
			return false;
		}
    	return true;
    }
    
    public static final class ControlFlowGraph {
        public final Edge edges[];
        public final Node nodes[];

        @JsonCreator
        public ControlFlowGraph(@JsonProperty("edges") Edge[] edges, @JsonProperty("nodes") Node[] nodes){
            this.edges = edges;
            this.nodes = nodes;
        }
        
        //find node based on vertex ID;
        public Node findNodeByVertexID(long vertexID){
        	for(int i=0; i<nodes.length; i++){
        		if(nodes[i].vertexID==vertexID){
        			return nodes[i];
        		}
        	}
        	return null;
        }
        
        public Node getNextNode(Node currentNode){
        	for(int i=0; i<edges.length; i++){
        		if(edges[i].source==currentNode.vertexID){
        			if(edges[i].condition==null){
        				edges[i].setPassed();
        				return findNodeByVertexID(edges[i].target);
        			}else{
        				System.out.println("Need to Execute The Current Node"+currentNode);
        			}
        		}
        	}
        	return null;
        	
        }
        public Node getNextNode(MSExecution ms){
        	for(int i=0; i<edges.length; i++){
        		if(edges[i].source==ms.MSNode.vertexID){
        			if(edges[i].condition==null | MSExecution.checkCondition(edges[i].condition , ms.getResult())==true){
        				ms.ParseResult(edges[i]);
        				edges[i].setPassed();
        				return findNodeByVertexID(edges[i].target);
        			}else{
        				//System.out.println("Fail to satisfy condition of "+edges[i].target);
        			}
        		}
        	}
        	return null;
        }
        
        public Node getStartNode(){
        	for(int i=0; i<nodes.length; i++){
        		if(nodes[i].nodeType.equals("startExecution")){
        			return nodes[i];
        		}
        	}
        	return null;
        }
        // find all parallel execution nodes under a parallel start node;
        public ArrayList<Node> findParallelNodes(Node parallelStartNode){
        	if(!parallelStartNode.nodeType.equals("startParallel")){
        		throw new Error("findParallelNodes must be applied to paral start node");
        	}
        	ArrayList<Node> returnNodes = new ArrayList<Node>();
        	for(int i=0; i<edges.length; i++){
        		if(edges[i].source==parallelStartNode.vertexID){
        			edges[i].setPassed();
        			returnNodes.add(findNodeByVertexID(edges[i].target));
        		}
        	}
        	return returnNodes;
        }
        
        public Node getParallelEndNode(Node parallelStart){
        	if(!parallelStart.nodeType.equals("startParallel")){
        		throw new Error("getParallelEndNode must be applied to paral start node");
        	}
        	Long parallelEndVertexID = Long.parseLong(parallelStart.microServiceTask);
        	Node parallelEnd = findNodeByVertexID(parallelEndVertexID);
        	if(!parallelEnd.nodeType.equals("endParallel")){
        		throw new Error("getParallelEndNode execution error");
        	}
        	return parallelEnd;
        }
        
        public static final class Edge {
            public final long source;
            public final long target;
            public final String condition;
            public final String param;
            public boolean passed;
    
            @JsonCreator
            public Edge(@JsonProperty("source") long source, @JsonProperty("target") long target, @JsonProperty("condition") String condition, @JsonProperty("param") String param){
                this.source = source;
                this.target = target;
                this.condition = condition;
                this.param = param;
                this.passed = false;
            }
            
            public void setPassed(){
            	this.passed = true;
            }
        }

        public static final class Node {
            public final long vertexID;
            public final String nodeType;
            public final String requiredInput; // requiredParams as String
            public final String params; // pre-set execution params;
            public final String microServiceTask;
            public final String device;
            public String[] requiredParams;
            public Map<String, String> priorExecutionResults;
            public boolean passed;
    
            @Override
            public String toString(){
            	return "\n vertexID:"+this.vertexID+
            			"\n nodeType:" + nodeType+
            			"\n requiredInput:"+requiredInput+
            			"\n microServiceTask:" + microServiceTask + "\n";           			
            }
            
            @JsonCreator
            public Node(@JsonProperty("vertexID") long vertexID, @JsonProperty("nodeType") String nodeType, @JsonProperty("requiredInput") String requiredInput, @JsonProperty("params") String params, @JsonProperty("microServiceTask") String microServiceTask, @JsonProperty("device") String device){
                this.vertexID = vertexID;
                this.nodeType = nodeType;
                this.requiredInput = requiredInput;
                this.params = params;
                this.microServiceTask = microServiceTask;
                this.device = device;
                this.priorExecutionResults = new HashMap<String, String>();
                this.requiredParams = parseRequiredParam();
                this.passed = false;
            }
            
            public void setPassed(){
            	this.passed = true;
            }
            public void addPriorExecutionResult(Map<String, String> result){
            	// it must not exist in pirorExecutionResults, and it must be in requiredParams;
            	final List<String> requiredParamList =  new ArrayList<String>();
				Collections.addAll(requiredParamList, requiredParams);
        		for (Map.Entry<String, String> entry : result.entrySet()) {
        			if(!priorExecutionResults.containsKey(entry.getKey())){
        				//if(requiredParamList.contains(entry.getKey()))
        				priorExecutionResults.put(entry.getKey(), entry.getValue());
        			}
        		}
            }
            
            public String[] parseRequiredParam(){
            	String[] params = requiredInput.split("\\|");
                List<String> list = new ArrayList<String>(Arrays.asList(params));
                list.removeAll(Collections.singleton(null));
                list.remove("");
                return list.toArray(new String[list.size()]);
            }
            
            public Map<String, String> parseInputParam(){
            	//System.out.print("Params"+params+"\n");
            	String[] inputParams = params.split("\\|");
                List<String> list = new ArrayList<String>(Arrays.asList(inputParams));
                list.removeAll(Collections.singleton(null));
                Map<String, String> returnInputParams = new HashMap<String, String>();
                for(int i=0;i<list.size();i++){
                	if(list.get(i).trim().equals("")) continue;
                	String[] tmp= list.get(i).split(":",2);
                	if(tmp[1].equals("__G__")){
                		tmp[1] = MicroService.globalParams.get(tmp[0]);
                		//System.out.println(tmp[1]);
                	}
                	returnInputParams.put(tmp[0], tmp[1]);
                }
                return returnInputParams;
            }
        }
    }
}