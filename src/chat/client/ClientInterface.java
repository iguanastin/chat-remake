package chat.client;

import chat.common.Event;
import chat.common.Message;

/**
 * Created by Austin on 4/16/2017.
 */
public interface ClientInterface {

    void messageReceived(Message message);

    void eventReceived(Event event);

}
