package deluge.net;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResponseExecutor
{

    ExecutorService mExecutor;
    
    public ResponseExecutor()
    {
        mExecutor = Executors.newFixedThreadPool(20);
    }
    
    public void shutdown()
    {
        if(mExecutor != null)
        {
            mExecutor.shutdown();
            while (!mExecutor.isTerminated())
            {
            }
            System.out.println("Finished all threads");               
        }     
    }
    
    public void execute(Runnable task)
    {
        mExecutor.execute(task);
    }
}
