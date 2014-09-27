package deluge.message;

public class Response
{
    private Object[] returnValue;
    private Error error;

    public Response(Object[] value, Error error)
    {
        this.returnValue = value;
        this.error = error;
    }
    
    public Response(Error error)
    {
        this(null, error);
    }
    
    public Response(Object[] value)
    {
        this(value, null);
    }

    public boolean hasError()
    {
        return error != null;
    }

    public Error getError()
    {
        return error;
    }

    public Object[] getReturnValue()
    {
        return returnValue;
    }
    
    @Override
    public String toString()
    {
        String str = "Response { ";
        for(int i=0; i<returnValue.length; i++)
        for(Object obj : returnValue)
        {
            str += obj.toString();
            if(i<returnValue.length-1)
                str += ",";            
        }            
        str += " }";
        return str;
    }
}
