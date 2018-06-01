package msExecution;


public class executor {
	public static void main(String[] args) {
		json2MS j2ms = new json2MS().readDefaultFile();
		MicroService ms = j2ms.convertWithoutGraph();
		ExecutorThread mainThread = new ExecutorThread(null,ms.controlFlowGraph.getStartNode());
		mainThread.setCFG(ms.controlFlowGraph);
        Thread t = new Thread(mainThread);
        t.start();  
	}
}
