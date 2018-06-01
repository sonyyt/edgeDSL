package msExecution;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.text.html.HTMLDocument.Iterator;

//import com.json.parsers.JSONParser;
//import com.json.parsers.JsonParserFactory;

import org.json.*;

import msExecution.MicroService.ControlFlowGraph.*;

// MS Execution of using Dicer Simulator

public class MSExecution {
    Node MSNode;
    String serviceExecutionPort = "";
    Map<String, String> executionResult = null;
    Map<String, String> executionParams = null;

    public MSExecution(Node msNode) {
        this.MSNode = msNode;
        this.executionResult = new HashMap<String, String>();
        //1. parse Input Params: get value of input params;
        //2. get required params: make sure that all params have been received;
        this.executionParams = MSNode.parseInputParam();
        if (!MSNode.priorExecutionResults.isEmpty()) {
            for (Map.Entry<String, String> entry : MSNode.priorExecutionResults.entrySet()) {
                this.setExecutionParams(entry.getKey(), entry.getValue());
            }
        }
    }

    // find a proper device by MySQL
    public void selectDevice() throws Exception {
        String deviceSelectionReules = MSNode.device;

        //this.serviceExecutionPort = "http://localhost:8888/microservices/task.php?microservice=" + MSNode.microServiceTask;
        String url = "http://localhost:8890/gateway/device.php?device=";

        url += URLEncoder.encode(deviceSelectionReules, "UTF-8");

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        // add request header
        con.setRequestProperty("User-Agent", "Mozilla/5.0");

        int responseCode = con.getResponseCode();
        //System.out.println("\n"+this.MSNode+"---executing MS: "+ MSNode.microServiceTask + " at URL : " + url);
        //System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        in.close();

        String device = response.toString().split("\\|")[0];
        String type = response.toString().split("\\|")[1];

        if (type.equals("Phone")) {
            this.serviceExecutionPort = device + "/microservices/" + this.MSNode.microServiceTask + "?1=1";
        }
        else {
            this.serviceExecutionPort = device + "/microservices/ms.php?service=" + this.MSNode.microServiceTask;
        }

    }

    // get the execution result of other MS nodes;
    // 1. remove from requiredParams;
    // 2. add to executionParams;
    public void setExecutionParams(String paramName, String paramValue) {
        final List<String> requiredParamList = new ArrayList<String>();
        Collections.addAll(requiredParamList, MSNode.requiredParams);
        if (requiredParamList.contains(paramName)) {
            requiredParamList.remove(requiredParamList.indexOf(paramName));
            executionParams.put(paramName, paramValue);
        }
        MSNode.requiredParams = requiredParamList.toArray(new String[requiredParamList.size()]);
    }

    public String conductURL() {
        try {
            selectDevice();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String executionURL = this.serviceExecutionPort;
        //System.out.println(executionURL);
        for (Map.Entry<String, String> entry : executionParams.entrySet()) {
            executionURL += "&" + entry.getKey() + "=" + entry.getValue();
        }

        return executionURL;
    }

    //establish HTTP connection to the selected device;
    public boolean execute() throws IOException, InterruptedException {
        String url = conductURL();

        System.out.println("\n" + this.MSNode + "---executing MS: " + MSNode.microServiceTask + " at URL : " + url);

        URL obj = new URL(url);

        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setConnectTimeout(3000);
        con.setReadTimeout(3000);

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", "Mozilla/5.0");

        int responseCode = con.getResponseCode();

        //System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //System.out.println("HTTP Fetch Result"+response.toString());
        if (response.toString().equals("")) {
            executionResult.put("status", "fail");
        } else {
            JSONObject json = new JSONObject(response.toString());

            for (String key : json.keySet()) {
                executionResult.put(key, json.get(key).toString());
            }
        }
        return executionResult.get("status").equals("success");
    }

    public boolean fakeExecute() {
        executionResult.put("status", "fail");
        return false;
    }

    public Map<String, String> getResult() {
        return executionResult;
    }


    public void ParseResult(Edge nextExecutionEdge) {
        String[] params = nextExecutionEdge.param.split("\\|");
        List<String> list = new ArrayList<String>(Arrays.asList(params));
        list.removeAll(Collections.singleton(null));
        list.remove("");

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) != null && list.get(i) != "") {
                String[] temp = list.get(i).split(":");
                String paramName = temp[0];
                String paramOutputName = temp[1];
                if (executionResult.containsKey(paramName)) {
                    String value = executionResult.get(paramName);
                    executionResult.remove(paramName);
                    executionResult.put(paramOutputName, value);
                }
            }
        }
    }

    //given the execution result, check whether it fits the condition to goto the next node;
    public static boolean checkCondition(String condition, Map<String, String> executionResult) {
        //System.out.print("Parsing Execution Result:");
        if (condition == null) {
            return true;
        }
        char conditionType = condition.charAt(0);

        if (conditionType == '1') {
            if (executionResult.get("status").equals("success")) {
                //System.out.println("Condition Fit: conditiontype"+conditionType+"---> execution status"+executionResult.get("status"));
                return true;
            }
        } else if (conditionType == '2') {
            if (executionResult.get("status").equals("fail")) {
                //System.out.println("Condition Fit: conditiontype"+conditionType+"---> execution status"+executionResult.get("status"));
                return true;
            }
        } else {
            String[] moreInfo = condition.substring(1).split("\\|");
            String param = moreInfo[0];
            String value = moreInfo[1];
            if (conditionType == '3') {
                if (Integer.parseInt(executionResult.get(param)) > Integer.parseInt(value)) {
                    return true;
                }
            } else if (conditionType == '4') {
                if (Integer.parseInt(executionResult.get(param)) >= Integer.parseInt(value)) {
                    return true;
                }
            } else if (conditionType == '5') {
                if (Integer.parseInt(executionResult.get(param)) == Integer.parseInt(value)) {
                    return true;
                }
            } else if (conditionType == '6') {
                if (Integer.parseInt(executionResult.get(param)) <= Integer.parseInt(value)) {
                    return true;
                }
            } else if (conditionType == '7') {
                if (Integer.parseInt(executionResult.get(param)) < Integer.parseInt(value)) {
                    return true;
                }
            } else if (conditionType == '8') {
                if (executionResult.get(param).equals(value)) {
                    return true;
                }
            }

        }
        return false;
    }
}

