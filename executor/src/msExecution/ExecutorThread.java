package msExecution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import msExecution.MicroService.*;
import msExecution.MicroService.ControlFlowGraph.*;

public class ExecutorThread implements Runnable {
    static ControlFlowGraph cfg = null;
    static Map<String, String> returnArray = null;
    CountDownLatch latchFromFather = null;
    int parentPEID = 0; // parent paralle end vertex ID ---- only parallel execution node created by parallel start node will have parentPEID;
    Node currentNode = null;
    CountDownLatch latch = null;


    public void setCFG(ControlFlowGraph cfg) {
        ExecutorThread.cfg = cfg;
        returnArray = new HashMap<String, String>();
    }

    public ExecutorThread(CountDownLatch latchToCountDown, Node currentNode, int parentPEID) {
        this.currentNode = currentNode;
        this.latchFromFather = latchToCountDown;
        this.parentPEID = parentPEID;
    }

    public ExecutorThread(CountDownLatch latchToCountDown, Node currentNode) {
        this.currentNode = currentNode;
        this.latchFromFather = latchToCountDown;
        this.parentPEID = 0;
    }

    @Override
    public void run() {
        try {
            execute();
        } catch (InterruptedException x) {
            System.out.println("interrupted while in workMethod()");
        }

    }

    public void execute() throws InterruptedException {
        while (true) {
            //System.out.println(currentNode);
            if (currentNode.nodeType.equals("startExecution")) {
                currentNode = cfg.getNextNode(currentNode);
            }
            if (currentNode.nodeType.equals("microService")) {
                Boolean result = false;
                MSExecution ms = new MSExecution(currentNode);
                try {
                    result = ms.execute();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //System.out.println(ms.MSNode.microServiceTask);
                if (cfg.getNextNode(ms) == null) {
                    break;
                } else {
                    currentNode = cfg.getNextNode(ms);
                }
                if (result == true) {
                    currentNode.addPriorExecutionResult(ms.getResult());
                }
            }
            //NOTICE HERE:  two types of end execution: success and failue.
            else if (currentNode.nodeType.contains("EndExecution")) {
                //System.out.println("Finished Overall Execution");
                String response = null;
                try {
                    response = new ObjectMapper().writeValueAsString(currentNode.priorExecutionResults);
                } catch (JsonProcessingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //System.out.println("Execution Status:"+currentNode.nodeType);
                System.out.println(response);
                //drawGraph dg = new drawGraph(cfg);
                //dg.parseExecutionGraph();

//				System.out.println("线程号： "+ Thread.currentThread().getName());
//			    ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
//			    int noThreads = currentGroup.activeCount();
//			    Thread[] lstThreads = new Thread[noThreads];
//			    currentGroup.enumerate(lstThreads);
//			    for (int i = 0; i < noThreads; i++){
//			    	System.out.println("线程号：" + i + " = " + lstThreads[i].getName());
//			    	if(!lstThreads[i].getName().equals(Thread.currentThread().getName()) && !lstThreads[i].getName().equals("DestroyJavaVM")){
//			    		lstThreads[i].interrupt();
//			    		try {
//							lstThreads[i].join();
//						} catch (InterruptedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//			    	}
//			    }
                System.exit(0);
                break;
            }
            //haven't processed the data requirement yet.

            else if (currentNode.nodeType.equals("startParallel")) {
                ArrayList<Node> parallelNodes = cfg.findParallelNodes(currentNode);
                Node paralleEnd = cfg.getParallelEndNode(currentNode);
                //processing parameters:
                // where is the execution params from the previous node?
                Map<String, String> providedParams = currentNode.priorExecutionResults;
                final List<String> requiredParamList = new ArrayList<String>();
                Collections.addAll(requiredParamList, cfg.getNextNode(paralleEnd).requiredParams); //parallel end?
                for (String providedParam : providedParams.keySet()) {
                    if (requiredParamList.contains(providedParam)) {
                        requiredParamList.remove(requiredParamList.indexOf(providedParam));
                    }
                }
                paralleEnd.requiredParams = requiredParamList.toArray(new String[requiredParamList.size()]);

                int ParallelParamSize = paralleEnd.requiredParams.length;
                latch = new CountDownLatch(ParallelParamSize);
                for (int i = 0; i < parallelNodes.size(); i++) {
                    ExecutorThread newExecutor = new ExecutorThread(latch, parallelNodes.get(i), (int) paralleEnd.vertexID);
                    // need to pass some more information to the thread, paral start node information;
                    new Thread(newExecutor).start();
                }
                try {
                    latch.await(3, TimeUnit.SECONDS);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                currentNode = paralleEnd;
            } else if (currentNode.nodeType.equals("endParallel")) {
                if (currentNode.vertexID == parentPEID) {
                    //System.out.println("---Thread:"+Thread.currentThread().getId()+"I am the child");
                    // I am the child thread, I should finish execution.
                    //
                    final List<String> requiredParamList = new ArrayList<String>();
                    Collections.addAll(requiredParamList, currentNode.requiredParams);
                    // the later execution result is dumped directly now...
                    for (String requiredParam : requiredParamList) {
                        //System.out.println("Thread:"+Thread.currentThread().getId()+"--requiredParam:"+requiredParam);
                        for (Map.Entry<String, String> entry : currentNode.priorExecutionResults.entrySet()) {
                            //System.out.println("prior execution results:" + entry.getKey() + ":" + entry.getValue());
                        }
                        if (currentNode.priorExecutionResults.containsKey(requiredParam)) {
                            requiredParamList.remove(requiredParamList.indexOf(requiredParam));
                            //System.out.println("counter:"+latchFromFather.getCount());
                            latchFromFather.countDown();
                        }
                        if (latchFromFather.getCount() == 0) {
                            break;
                        }
                    }
                    currentNode.requiredParams = requiredParamList.toArray(new String[requiredParamList.size()]);
                    break;
                } else if (currentNode.vertexID != parentPEID) {
                    if (latch.getCount() != 0) {
                        Node nextNode = cfg.getNextNode(currentNode);
                        currentNode = nextNode;
                        MSExecution ms = new MSExecution(currentNode);
                        ms.fakeExecute();
                        currentNode = cfg.getNextNode(ms);
                        currentNode.addPriorExecutionResult(ms.getResult());
                    } else {
                        //System.out.println("I am the parent");
                        // I am the father thread, I should continue execution.
                        Node nextNode = cfg.getNextNode(currentNode);
                        nextNode.addPriorExecutionResult(currentNode.priorExecutionResults);
                        currentNode = nextNode;
                    }
                    // unconditional transfer.
                    // TODO: need to transfer ms execution results as input data!!!!
                }
            }

        }
    }
}
