package backup;

import java.util.Map;


import msExecution.MicroService.ControlFlowGraph.*;


public class MSExecution {
	Node MSNode;
	String device = "";
	String[] executionResult = null;
	Map<String, String> params = null;
	
	public MSExecution(Node msNode){
		this.MSNode = msNode;
	}
	
	// find a proper device by MySQL
	public void selectDevice() {
		String deviceSelectionReules = MSNode.device;
		this.device = null;
	}
	
	// other rules!!! to specify
	public void setExecutionParams(String params){
		this.params = null;
	}
	
	//establish HTTP connection to the selected device;
	public boolean execute(){
		return false;
	}
	
	public int next(){
		return 0;
	}
}

