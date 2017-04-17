package chat.client;

import chat.common.Event;
import chat.common.Message;
import chat.common.Sendable;
import ocsf.client.AbstractClient;

import java.io.IOException;

public class ChatClient extends AbstractClient {

    private String id = "";
    private boolean loggedIn = false;

    private ClientInterface ui;


    public ChatClient(String host, int port) {
        super(host, port);
    }

    public ChatClient() {
        super(null, -1);
    }

    //------------------ Functionality ---------------------------------------------------------------------------------

    public void forceDisconnect() throws IOException {
        if (isConnected()) {
            loggedIn = false;
            closeConnection();
        }
    }

    public void cleanDisconnect() throws IOException {
        if (isConnected()) {
            sendToServer(new Event(id, Event.EVENT_DISCONNECT));
            forceDisconnect();
        }
    }

    public void connect(String id, String password) throws IOException {
        this.id = id;

        openConnection();
        sendToServer(new Event(Event.EVENT_LOGIN_REQUEST, new String[]{id, password}));
    }

    public boolean hasInterface() {
        return ui != null;
    }

    public void setInterface(ClientInterface ui) {
        this.ui = ui;
    }

    public String getId() {
        return id;
    }

    public boolean isLoggedIn() {
        return loggedIn;
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
                forceDisconnect();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else if (event.getType() == Event.EVENT_LOGIN_SUCCESS) {
            loggedIn = true;
        }

        if (hasInterface()) ui.eventReceived(event);
    }

}
