package deluge.message;

public class Event
{
    String name;
    Object[] data;
    
    public Event(String name, Object[] data)
    {
        this.name = name;
        this.data = data;
    }
    
    public String getName()
    {
        return name;
    }

    public Object[] getData()
    {
        return data;
    }
}
