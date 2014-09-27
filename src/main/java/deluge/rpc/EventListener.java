package deluge.rpc;

import deluge.message.Event;

public interface EventListener
{
    public void event(Event msg);
}
