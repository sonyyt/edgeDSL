package msExecution;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;  
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.text.html.HTMLDocument.Iterator;

import com.json.parsers.JSONParser;
import com.json.parsers.JsonParserFactory;

import msExecution.MicroService.ControlFlowGraph.*;

// MS Execution of using Dicer Simulator

public class MSExecution {
	Node MSNode;
	String serviceExecutionPort = "";
	Map<String, String> executionResult = null;
	Map<String, String> executionParams = null;
	
	public MSExecution(Node msNode){
		this.MSNode = msNode;
		this.executionResult = new HashMap<String, String>();
		//1. parse Input Params: get value of input params;
		//2. get required params: make sure that all params have been received; 
		this.executionParams = MSNode.parseInputParam();
		if(!MSNode.priorExecutionResults.isEmpty()){
    		for (Map.Entry<String, String> entry : MSNode.priorExecutionResults.entrySet()) {
    			this.setExecutionParams(entry.getKey(), entry.getValue());
    			}
    		}
	}
	
	// find a proper device by MySQL
	public void selectDevice() {
		String deviceSelectionReules = MSNode.device;
		// should do: use MySQL to query device; 
		this.serviceExecutionPort = "http://localhost/microservice/task.php?microservice="+MSNode.microServiceTask;
	}
	
	// get the execution result of other MS nodes; 
	// 1. remove from requiredParams;
	// 2. add to executionParams;
	public void setExecutionParams(String paramName, String paramValue){
		final List<String> requiredParamList =  new ArrayList<String>();
		Collections.addAll(requiredParamList, MSNode.requiredParams);
		if(requiredParamList.contains(paramName)){
			requiredParamList.remove(requiredParamList.indexOf(paramName));
			executionParams.put(paramName, paramValue); 
		}
		MSNode.requiredParams = requiredParamList.toArray(new String[requiredParamList.size()]);
	}
	
	public String conductURL(){
		selectDevice();
		String executionURL = this.serviceExecutionPort;
		//System.out.println(executionURL);
		for (Map.Entry<String, String> entry : executionParams.entrySet()) {
			executionURL += "&"+entry.getKey()+"="+entry.getValue();
		}
		
		return executionURL;
	}
	
	//establish HTTP connection to the selected device;
	public boolean execute() throws IOException{
		String url = conductURL();

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		//add request header
		con.setRequestProperty("User-Agent", "Mozilla/5.0");

		int responseCode = con.getResponseCode();
		System.out.println("\n"+this.MSNode+"---executing MS: "+ MSNode.microServiceTask + " at URL : " + url);
		//System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		System.out.println("HTTP Fetch Result"+response.toString());
		JsonParserFactory factory=JsonParserFactory.getInstance();
		JSONParser parser=factory.newJsonParser();
		Map<String, String> jsonMap=parser.parseJson(response.toString());
		
		for (Entry<String, String> entry : jsonMap.entrySet()) {
			executionResult.put( entry.getKey(), entry.getValue());
		}	
		return executionResult.get("status").equals("success");
	}

	public Map<String, String> getResult(){
		return executionResult;
	}
	
	
	
	public void ParseResult(Edge nextExecutionEdge){
    	String[] params = nextExecutionEdge.param.split("\\|");
        List<String> list = new ArrayList<String>(Arrays.asList(params));
        list.removeAll(Collections.singleton(null));
        list.remove("");
        
        for(int i=0; i<list.size();i++){
        	if(list.get(i)!=null && list.get(i)!=""){
        		String[] temp = list.get(i).split(":");
        		String paramName = temp[0];
        		String paramOutputName = temp[1];
        		if(executionResult.containsKey(paramName)){
        			String value = executionResult.get(paramName);
        			executionResult.remove(paramName);
        			executionResult.put(paramOutputName, value);
        		}
        	}
        }
	}
	
	//given the execution result, check whether it fits the condition to goto the next node;
	public static boolean checkCondition(String condition, Map<String, String> executionResult){
		System.out.print("Parsing Execution Result:");
		if(condition==null){
			return true;
		}
		char conditionType = condition.charAt(0);
		
		if(conditionType=='1'){
			if(executionResult.get("status").equals("success")){
				System.out.println("Condition Fit: conditiontype"+conditionType+"---> execution status"+executionResult.get("status"));
				return true;
			}
		}else if(conditionType=='2'){
			if(executionResult.get("status").equals("fail")){
				System.out.println("Condition Fit: conditiontype"+conditionType+"---> execution status"+executionResult.get("status"));
				return true;
			}
		}
		else{
			String[] moreInfo = condition.substring(1).split("\\|");
			String param = moreInfo[0];
			String value = moreInfo[1];
			if(conditionType=='3'){
				if(Integer.getInteger(param)>Integer.getInteger(value)){
					return true;
				}
			}else if(conditionType=='4'){
				if(Integer.getInteger(param)>=Integer.getInteger(value)){
					return true;
				}
			}else if(conditionType=='5'){
				if(Integer.getInteger(param)==Integer.getInteger(value)){
					return true;
				}
			}else if(conditionType=='6'){
				if(Integer.getInteger(param)<=Integer.getInteger(value)){
					return true;
				}
			}else if(conditionType=='7'){
				if(Integer.getInteger(param)<Integer.getInteger(value)){
					return true;
				}
			}else if(conditionType=='8'){
				if(param.equals(value)){
					return true;
				}
			}
			
		}
		return false;
	}
}

