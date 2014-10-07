package deluge.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;

import javax.net.ssl.SSLSocket;

import deluge.message.Event;

public class Connection
{
    public interface DataCallback
    {
        public void dataRecived(byte[] data);
    }
    
    private BlockingQueue<byte[]> queue = new ArrayBlockingQueue<byte[]>(50);
    
    private SSLSocket mySocket;
    String myAddress;
    int myPort;
    Thread sender = null;
    public final CountDownLatch latch = new CountDownLatch(1);
    
    public Connection(String address, int port)
    {
        myAddress = address;
        myPort = port;
    }

    private void createSocket()
    {
        if(mySocket == null)
        {
            try
            {
                mySocket = SSL3Socket.createSSLv3Socket(myAddress, myPort);
                mySocket.startHandshake();
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
                mySocket = null;
            }
        }
        latch.countDown();
    }
    
    public void send(byte[] request) throws IOException
    {
        if(sender == null)
        {
            sender();
        }
        try
        {
            queue.put(request);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public void sender() throws IOException
    {   
        sender =  new Thread(new Runnable() {
            
            public void run()
            {
                createSocket();
                System.out.println("Sending Thread started");
                try
                {                
                    while(mySocket != null)
                    {
                        byte[] packedData;
                        try
                        {
                            byte[] x = queue.take();
                            packedData = compress(x);

                            OutputStream out = mySocket.getOutputStream();
                            out.write(packedData);
                            out.flush();
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                        
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });
        sender.start();

        try
        {
            latch.await(3, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        { }
        if(mySocket == null)
        {
            throw new IOException();            
        }
    }
    
    public void listen(final DataCallback cb) throws IOException
    {        
        new Thread(new Runnable() {
            
            public void run()
            {
                createSocket();
                System.out.println("Listening Thread started");
                try
                {
                    while(mySocket != null)
                    {
                        InputStream inputStream = mySocket.getInputStream();
                        
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        
                        int bytesRead;
                        byte[] buffer = new byte[1024];
                        while ((bytesRead = inputStream.read(buffer)) != -1)
                        {
                            try
                            {
                                baos.write(buffer);
                                                              
                                if(bytesRead < 1024)
                                {
                                    byte[] unpacked = decompress(baos.toByteArray());
                                    baos.reset();
                                    cb.dataRecived(unpacked);
                                }
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
        

        try
        {
            latch.await(3, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        { }
        if(mySocket == null)
        {
            throw new IOException();            
        }
    }
    
    public byte[] compress(byte[] data) throws IOException
    {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        Deflater d = new Deflater();
        DeflaterOutputStream dout = new DeflaterOutputStream(baos, d);
        dout.write(data);
        dout.close();
        
        byte[] output = baos.toByteArray();
        baos.close();
        return output;
    }

    private static byte[] decompress(byte[] input) throws DataFormatException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        Inflater decompresser = new Inflater(false);

        decompresser.setInput(input, 0, input.length);
        byte[] result = new byte[1024];
        while(!decompresser.finished())
        {
            int resultLength = decompresser.inflate(result);
            baos.write(result, 0, resultLength);            
        }
        decompresser.end();
        
        byte[] returnValue = baos.toByteArray();
        try
        {
            baos.close();
        }
        catch (IOException e) { }
        return returnValue;
    }

}
