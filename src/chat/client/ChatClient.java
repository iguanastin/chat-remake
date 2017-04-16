package chat.client;

import chat.common.Event;
import chat.common.Message;
import chat.common.Sendable;
import ocsf.client.AbstractClient;

import java.io.IOException;

public class ChatClient extends AbstractClient {

    private String id = "";

    private ClientInterface ui;


    public ChatClient(String host, int port) {
        super(host, port);
    }

    public ChatClient() {
        super(null, -1);
    }

    //------------------ Functionality ---------------------------------------------------------------------------------

    private void disconnect() throws IOException {
        if (isConnected()) {
            closeConnection();
        }
    }

    private void sendAndDisconnect() throws IOException {
        if (isConnected()) {
            sendToServer(new Event(id, Event.EVENT_DISCONNECT));
            disconnect();
        }
    }

    private void connect(String password) throws IOException {
        openConnection();
        sendToServer(new Event(id, Event.EVENT_LOGIN, new String[]{id, password}));
    }

    public boolean hasInterface() {
        return ui != null;
    }

    public void setInterface(ClientInterface ui) {
        this.ui = ui;
    }

    public boolean setId(String id) {
        if (!isConnected()) {
            this.id = id;
            return true;
        }

        return false;
    }

    //-------------- Handlers ------------------------------------------------------------------------------------------

    @Override
    protected void handleMessageFromServer(Object msg) {
        if (msg instanceof Sendable) {
            if (msg instanceof Event) {
                handleEventFromServer((Event) msg);
            } else if (msg instanceof Message) {
                handleActualMessageFromServer((Message) msg);
            }
        } else {
            System.err.println("Received non-Sendable object: " + msg);
        }
    }

    private void handleActualMessageFromServer(Message msg) {


        if (hasInterface()) ui.messageReceived(msg);
    }

    private void handleEventFromServer(Event event) {
        if (event.getType() == Event.EVENT_DISCONNECT) {
            try {
                disconnect();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        if (hasInterface()) ui.eventReceived(event);
    }

}
