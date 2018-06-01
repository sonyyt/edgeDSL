package backup;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Test {
	
	
	
    public static void main(String[] args) throws Exception {
    	
    	/*wThread
    	 * input thread
    	 * 
    	 * func exec： will execute till it needs more threads. 
    	 * while ( node. next. type == normal node ){
    	 *      node.next.execute() 
    	 * }
    	 * while (node.next.type = parallel start){
    	 * 		node-thread-id = node.id    
    	 *      latch = new CountDownLatch(children.amount);
    	 * 		forall child = node.next.children
    	 * 			new wThread.exec(node.next.child, node-thread-id  
    	 * )
    	 * 		latch.await();
    	 *      node = parallel.end; 
    	 * }
    	 * while (node.next.type = parallel end){
    	 * 		// I am not the parallel executing node
    	 * 		if(	node-thread-id ! = node.next.id ){
    	 * 			check execution result, find next node. 
    	 * 		}	
    	 * 		else{
    	 * 			this.latch.countDown();
    	 * 		}
    	 * }
    	 * 
    	 * 
    	 * execute(node）
			if(node.type==normal)
				new thread to execute the node; 
			if(node.type = parallel_start)
				execute(children nodes)

		*/
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(new Task());

        try {
            System.out.println("Started..");
            System.out.println(future.get(3, TimeUnit.SECONDS));
            System.out.println("Finished!");
        } catch (TimeoutException e) {
            future.cancel(true);
            System.out.println("Terminated!");
        }

        executor.shutdownNow();
    }
}

class Task implements Callable<String> {
    @Override
    public String call() throws Exception {
        Thread.sleep(4000); // Just to demo a long running task of 4 seconds.
        return "Ready!";
    }
}