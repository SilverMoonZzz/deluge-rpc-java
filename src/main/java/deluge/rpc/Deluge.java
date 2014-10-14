package deluge.rpc;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import se.dimovski.rencode.Rencode;
import deluge.api.DelugeRPCCallback;
import deluge.message.MessageData;
import deluge.message.Request;
import deluge.message.Response;
import deluge.message.MessageData.MessageType;
import deluge.net.Connection;
import deluge.net.Connection.DataCallback;
import deluge.net.ResponseExecutor;
import deluge.net.TorrentField;

public class Deluge implements DataCallback
{
    private Connection conn;
    private EventQueue eventQueue;
    private ResponseExecutor mResponseExecutor;
    private Map<Integer, OngoingRequest> mOngoingRequests;
    
    private Deluge(Connection c) throws IOException
    {
        conn = c;
        eventQueue = new EventQueue();
        mOngoingRequests = new ConcurrentHashMap<Integer, OngoingRequest>();
        mResponseExecutor = new ResponseExecutor();
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
    
    public void dataRecived(final byte[] data)
    {
        mResponseExecutor.execute(new Runnable() {
            public void run()
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
    
                    OngoingRequest request = mOngoingRequests.remove(message.getRequestId());
                    if(request.getAsyncCallback() != null)
                    {                        
                        int requestId = request.getRequest().getRequestId();
                        request.getAsyncCallback().onResponse(requestId, response);
                    }
                    request.getFutureResponse().setResponse(response);
                }
            }
        });
    }
    
    public void addEventListener(EventListener listener)
    {
        eventQueue.subscribe(listener);
    }

    public Future<Response> send(Request request)
    {
        OngoingRequest ongoingRequest = new OngoingRequest(request);
        mOngoingRequests.put(request.getRequestId(), ongoingRequest);
        
        send(request.toByteArray());
        return ongoingRequest.getFutureResponse();
    }
    
    public void sendAsync(Request request, DelugeRPCCallback cb)
    {
        OngoingRequest ongoingRequest = new OngoingRequest(request, cb);
        mOngoingRequests.put(request.getRequestId(), ongoingRequest);
        
        send(request.toByteArray());
    }
    
    private void send(byte[] data)
    {
        try
        {
            conn.send(data);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }    

    public Future<Response> login(String username, String password)
    {
        Request req = new Request("daemon.login", Util.objects(username, password));  
        return send(req);
    }
    
    public Future<Response> info()
    {
        Request req = new Request("daemon.info");        
        return send(req);
    }

    // returns String[] with torrentIds
    public Future<Response> getSessionState()
    {
        Request req = new Request("core.get_session_state");        
        return send(req);
    }
    
    // filter<"State", "Active">
    // filter<"Id", String[] ids>
    public void getTorrentStatusAsync(Map<Object, Object> filter, TorrentField[] fields, DelugeRPCCallback cb)
    {
        Object[] fieldNames = new Object[fields.length];
        for(int i=0; i<fields.length; i++)
        {
            fieldNames[i] = fields[i].toString();
        }
        
        Request req = new Request("core.get_torrents_status", Util.objects(filter, fieldNames));        
        sendAsync(req, cb);
    }
    
    public Future<Response> getTorrentStatus(Map<Object, Object> filter, TorrentField[] fields)
    {
        Object[] fieldNames = new Object[fields.length];
        for(int i=0; i<fields.length; i++)
        {
            fieldNames[i] = fields[i].toString();
        }
        
        Request req = new Request("core.get_torrents_status", Util.objects(filter, fieldNames));        
        return send(req);
    }

    /*
     * encodedContents Base 64 encoded string of the torrent file contents.
     */
    public Future<Response> addTorrentFile(String name, String encodedContents, Map<String, Object> options)
    {
        Request req = new Request("core.add_torrent_file", Util.objects(name, encodedContents, options));
        return send(req);
    }

    public Future<Response> removeTorrent(String torrentId, Boolean removeData)
    {
        Request req = new Request("core.remove_torrent", Util.objects(torrentId, removeData));
        return send(req);
    }

    public Future<Response> pause_torrent(String[] torrentIds)
    {
        Request req = new Request("core.pause_torrent", Util.objects((Object)torrentIds));
        return send(req);
    }
    
    public Future<Response> resume_torrent(String[] torrentIds)
    {
        Request req = new Request("core.resume_torrent", Util.objects((Object)torrentIds) );
        return send(req);
    }
}
