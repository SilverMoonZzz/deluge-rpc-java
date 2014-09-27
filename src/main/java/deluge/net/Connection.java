package deluge.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.net.ssl.SSLSocket;

public class Connection
{
    public interface DataCallback
    {
        public void dataRecived(byte[] data);
    }
    
    private SSLSocket mySocket;
    String myAddress;
    int myPort;
    
    public Connection(String address, int port) throws UnknownHostException, IOException, KeyManagementException, NoSuchAlgorithmException
    {
        myAddress = address;
        myPort = port;

        mySocket = SSL3Socket.createSSLv3Socket(myAddress, myPort);
        mySocket.startHandshake();
    }
    
    public void send(byte[] request) throws IOException
    {
        byte[] packedData = compress(request);
        OutputStream out = mySocket.getOutputStream();
        out.write(packedData);
        out.flush();
    }
    
    public void listen(final DataCallback cb) throws IOException
    {        
        new Thread(new Runnable() {
            
            public void run()
            {
                System.out.println("Listening on socket");
                try
                {                
                    while(true)
                    {          
                        byte[] buffer = new byte[1024];
                        
                        InputStream inputStream = mySocket.getInputStream();
                        while (inputStream.read(buffer) != -1)
                        {
                            try
                            {
                                byte[] unpacked = decompress(buffer);
                                cb.dataRecived(unpacked);
                            }
                            catch (DataFormatException e)
                            {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }                    
                }
                catch (UnsupportedEncodingException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }).start();     
    }
    
    private static byte[] compress(byte[] input)
    {
        byte[] output = new byte[100];
        
        Deflater compresser = new Deflater(Deflater.DEFAULT_COMPRESSION);

        compresser.setInput(input);
        compresser.finish();
        int compressedDataLength = compresser.deflate(output);
        compresser.end();
        
        return Arrays.copyOf(output, compressedDataLength);
    }

    private static byte[] decompress(byte[] input) throws DataFormatException
    {
        Inflater decompresser = new Inflater();
        decompresser.setInput(input, 0, input.length);
        byte[] result = new byte[255];            
        int resultLength = decompresser.inflate(result);
        decompresser.end();
        return Arrays.copyOf(result, resultLength);
    }

}
