package deluge.message;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import se.dimovski.rencode.Rencode;

public class Request
{
    private static AtomicInteger requestCounter = new AtomicInteger();
    
    private Integer requestId;
    private String method;
    private Object[] args;
    private Map<Object, Object> kwargs;
    
    public Request(String method)
    {
        this(method, new Object[0]);
    }

    public Request(String method, Object[] args)
    {
        this(method, args, new HashMap<Object, Object>());
    }
    
    public Request(String method, Object[] args, Map<Object, Object> kwargs)
    {
        this.requestId = requestCounter.getAndIncrement();
        this.method = method;
        this.args = args;
        this.kwargs = kwargs;
    }

    public Integer getRequestId()
    {
        return requestId;
    }
    
    public byte[] toByteArray()
    {
        Object obj = new Object[] { new Object[] { requestId, method, args, kwargs } };
        return Rencode.encode(obj);
    }
}
