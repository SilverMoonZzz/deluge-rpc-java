package deluge.rpc;

import deluge.api.DelugeRPCCallback;
import deluge.message.FutureResponse;
import deluge.message.Request;

public class OngoingRequest
{
    Request mRequest;
    FutureResponse mFutureResponse;
    DelugeRPCCallback mCallback;

    public OngoingRequest(Request request)
    {
        mRequest = request;
        mFutureResponse = new FutureResponse();
    }
    
    public OngoingRequest(Request request, DelugeRPCCallback callback)
    {
        this(request);
        mCallback = callback;
    }

    public Request getRequest()
    {
        return mRequest;
    }

    public FutureResponse getFutureResponse()
    {
        return mFutureResponse;
    }
    
    public DelugeRPCCallback getAsyncCallback()
    {
        return mCallback;
    }
}
