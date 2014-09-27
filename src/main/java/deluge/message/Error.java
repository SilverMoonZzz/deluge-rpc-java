package deluge.message;

public class Error
{
    final public String exceptionType;
    final public String exceptionMsg;
    final public String traceback;
    
    public Error(String type, String msg, String trace)
    {
        exceptionType = type;
        exceptionMsg = msg;
        traceback = trace;
    }
    
    public static Error error(String type, String msg, String trace)
    {
        return new Error(type, msg, trace);
    }
}
