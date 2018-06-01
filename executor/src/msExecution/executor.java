package msExecution;

import java.util.HashMap;
import java.util.Map;

public class executor {

    public static void main(String[] args) {
        String msFile = "";
        Map<String, String> globalInput = new HashMap<>();

        for (String arg : args) {
            if (arg.contains("=")) {
                String[] tmp = arg.split("=");
                if (tmp[0].equals("ms")) {
                    msFile = tmp[1];
                } else {
                    globalInput.put(tmp[0], tmp[1]);
                }
            }
        }

        json2MS j2ms = new json2MS().readMSFile(msFile);
        MicroService ms = j2ms.convertWithoutGraph();

        if (!ms.addGlobalInput(globalInput)) {
            System.out.print("Some Input Params are Missing");
            System.exit(0);
        }

        ExecutorThread mainThread = new ExecutorThread(null, ms.controlFlowGraph.getStartNode());
        mainThread.setCFG(ms.controlFlowGraph);
        Thread t = new Thread(mainThread);
        t.start();
    }
}
