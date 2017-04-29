package chat.server;

import chat.common.Message;
import chat.common.events.Event;
import ocsf.server.ConnectionToClient;

/**
 * Created by Austin on 4/26/2017.
 */
public interface ServerInterface {

    void messageReceived(Message msg);

    void eventReceived(Event event);

    void clientConnected(String ip);

    void clientDisconnected(String ip);

    void clientDisconnected(String ip, String id);

    void clientLoggedIn(String ip, String id);

}
