package deluge.message;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import deluge.api.DelugeRPCCallback;

public class FutureResponse implements Future<Response>
{
    
    public DelugeRPCCallback callback;
    
    public final CountDownLatch latch = new CountDownLatch(1);
    
    private Response response = null;     
    
    public boolean cancel(boolean mayInterruptIfRunning)
    {
        // TODO Auto-generated method stub
        return false;
    }

    public Response get() throws InterruptedException, ExecutionException
    {
        latch.await();
        return response;
    }

    public Response get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
    {
        if(!latch.await(timeout, unit))
        {
            throw new TimeoutException();            
        }
        return response;
    }

    public boolean isCancelled()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isDone()
    {
        return response != null;
    }
    
    public void setResponse(Response res)
    {
        response = res;   
        latch.countDown();
    }

}
