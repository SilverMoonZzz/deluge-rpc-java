package deluge.api;

import deluge.message.Response;

public abstract class DelugeRPCCallback
{
    // Handling the servers response
    public abstract void onResponse(long requestId, Response response);
    
    // Handling any error in the library
    public void onError(long requestId, Error error)
    {   
    }

    // Handling an error response from the server
    public void onServerError(long requestId, Exception exception)
    {
    }
}
