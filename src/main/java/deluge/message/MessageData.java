package deluge.message;

public class MessageData
{
    public static enum MessageType
    {
        RPC_RESPONSE(1),
        RPC_ERROR(2),
        RPC_EVENT(3);
        
        private final int id;
        
        MessageType(int id)
        {
            this.id = id;
        }
        
        public int getValue()
        {
            return id;
        }
    }
    
    private Object[] data;
    
    private MessageData(Object[] msg)
    {
        data = msg;
    }
    
    public static MessageData build(Object[] msg)
    {
        return new MessageData(msg);
    }
    
    public int getMessageType()
    {
        return (Integer) data[0];
    }

    public boolean isType(MessageType eventType)
    {
        return getMessageType() == eventType.getValue();
    }

    public Event getEvent()
    {
        if(isType(MessageType.RPC_EVENT))
        {
            // [message_type, event_name, [data]]
            String name        = (String)data[1];
            Object[] eventData = (Object[])data[2];
            return new Event(name, eventData);
        }
        return null;
    }

    public Response getResponse()
    {
        if(isType(MessageType.RPC_RESPONSE))
        {
            // [message_type, request_id, [return_value]]
            Object[] returnValue = null;
            if(data[2] == null)
            {
                returnValue = new Object[0];
            }
            else if(data[2] instanceof Object[])
            {
                returnValue = (Object[])data[2];                
            }
            else
            {
                returnValue = new Object[] { data[2] };
            }
            return new Response(returnValue); 
        }
        else if(isType(MessageType.RPC_ERROR))
        {
            // [message_type, request_id, exception_type, exception_msg, traceback]
            String exceptionType = (String)data[2];
            String exceptionMsg  = (String)data[3];
            String traceback     = (String)data[4];
            Error err = Error.error(exceptionType, exceptionMsg, traceback);
            return new Response(err);
        }
        
        return null;
    }

    public int getRequestId()
    {
        // TODO: handle when incorrect type
        return (Integer) data[1];
    }
}
