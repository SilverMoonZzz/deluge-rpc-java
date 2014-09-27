package deluge.rpc;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import deluge.message.Event;

public class EventQueue
{
    private Queue<Event> queue = new ArrayBlockingQueue<Event>(20);
    
    private List<EventListener> listeners = new ArrayList<EventListener>();
    
    private static Thread thread;
    
    public EventQueue()
    {
        thread = new Thread(new Runnable() {
            
            public void run()
            {
                System.out.println("Message Queue Callback thread started");
                try
                {                
                    while(true)
                    {
                        Event event = queue.poll();
                        if(event != null)
                        {
                            for(EventListener listener : listeners)
                            {
                                listener.event(event);
                            }
                        }
                    }                    
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        });
        thread.start();
    }

    public void add(Event event)
    {
        queue.add(event);
    }
    
    public void subscribe(EventListener listener)
    {
        if(listener != null)
        {
            listeners.add(listener);            
        }
    }
    
    public void unsubscribe(EventListener listener)
    {
        if(listener != null)
        {
            listeners.remove(listener);            
        }
    }
}
