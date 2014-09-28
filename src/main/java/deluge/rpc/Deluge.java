package deluge.rpc;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import se.dimovski.rencode.Rencode;
import deluge.message.FutureResponse;
import deluge.message.MessageData;
import deluge.message.Request;
import deluge.message.Response;
import deluge.message.MessageData.MessageType;
import deluge.net.Connection;
import deluge.net.Connection.DataCallback;
import deluge.net.TorrentField;

public class Deluge implements DataCallback
{
    private Connection conn;
    private EventQueue eventQueue;
    
    private Map<Integer, FutureResponse> pendingRequests;

    private Deluge(Connection c) throws IOException
    {
        conn = c;
        eventQueue = new EventQueue();
        pendingRequests = new HashMap<Integer, FutureResponse>();
        conn.listen(this);
    }
    public static Deluge connect(String address) throws KeyManagementException, UnknownHostException, NoSuchAlgorithmException, IOException
    {
        String[] parts = address.split(":");
        int port = parts.length < 2 ? 58846 : Integer.parseInt(parts[1]);
        return connect(parts[0], port);
    }
    
    public static Deluge connect(String address, int port) throws KeyManagementException, UnknownHostException, IOException, NoSuchAlgorithmException
    {
        Connection connection = new Connection(address, port);        
        return new Deluge(connection);
    }
    
    public void dataRecived(byte[] data)
    {
        Object[] decodedData = (Object[])Rencode.decode(data);

        MessageData message = MessageData.build(decodedData);
        if(message.isType(MessageType.RPC_EVENT))
        {
            eventQueue.add( message.getEvent() );
        }
        else if(message.isType(MessageType.RPC_RESPONSE) || message.isType(MessageType.RPC_ERROR))
        {
            Response response = message.getResponse();            
            FutureResponse future = pendingRequests.remove(message.getRequestId());            
            future.setResponse(response);
        }
    }
    
    public void addEventListener(EventListener listener)
    {
        eventQueue.subscribe(listener);
    }

    private Future<Response> sendRequest(Request request)
    {  
        try
        {
            conn.send(request.toByteArray());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return createPendingRequest(request.getRequestId());
    }

    private Future<Response> createPendingRequest(int requestId)
    {
        FutureResponse future = new FutureResponse();
        pendingRequests.put(requestId, future);
        return future;
    }
    

    public Future<Response> login(String username, String password)
    {
        Request req = new Request("daemon.login", Util.objects(username, password));  
        return sendRequest(req);
    }
    
    public Future<Response> info()
    {
        Request req = new Request("daemon.info");        
        return sendRequest(req);
    }

    // returns String[] with torrentIds
    public Future<Response> getSessionState()
    {
        Request req = new Request("core.get_session_state");        
        return sendRequest(req);
    }
    
    // filter<"State", "Active">
    // filter<"Id", String[] ids>
    public Future<Response> getTorrentStatus(Map<Object, Object> filter, TorrentField[] fields)
    {
        Object[] fieldNames = new Object[fields.length];
        for(int i=0; i<fields.length; i++)
        {
            fieldNames[i] = fields[i].toString();
        }
        
        Request req = new Request("core.get_torrents_status", Util.objects(filter, fieldNames));        
        return sendRequest(req);
    }
    
    /*
     * encodedContents Base 64 encoded string of the torrent file contents.
     */
    public Future<Response> addTorrentFile(String name, String encodedContents, Map<String, Object> options)
    {
        Request req = new Request("core.add_torrent_file", Util.objects(name, encodedContents, options));
        return sendRequest(req);
    }
}
