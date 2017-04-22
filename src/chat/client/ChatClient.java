package chat.client;

import chat.common.events.*;
import chat.common.Message;
import chat.common.Sendable;
import ocsf.client.AbstractClient;

import java.io.IOException;

public class ChatClient extends AbstractClient {

    //---------------- Instance Variables ------------------------------------------------------------------------------

    private String id;

    private boolean loggedIn = false;
    private boolean loggingIn = false;

    private ClientInterface ui;


    //------------------ Constructors ----------------------------------------------------------------------------------

    public ChatClient(String host, int port) {
        super(host, port);
    }

    public ChatClient() {
        super(null, -1);
    }

    //------------------ Methods ---------------------------------------------------------------------------------------

    public void forceDisconnect() throws IOException {
        if (isConnected()) {
            loggedIn = false;
            loggingIn = false;
            closeConnection();
        }
    }

    public void cleanDisconnect() throws IOException {
        if (isConnected()) {
            sendToServer(new DisconnectEvent());
            forceDisconnect();
        }
    }

    public void connect(String id, String password) throws IOException {
        this.id = id;

        loggingIn = true;

        openConnection();
        sendToServer(new LoginEvent(id, password, LoginEvent.LOGIN_REQUEST));
    }

    //----------------- Getters/Setters --------------------------------------------------------------------------------

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

    public boolean isLoggingIn() {
        return loggingIn;
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
        if (event instanceof DisconnectEvent) {
            try {
                forceDisconnect();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else if (event instanceof LoginEvent) {
            LoginEvent login = (LoginEvent) event;

            if (login.getType() == LoginEvent.LOGIN_SUCCEED) {
                loggedIn = true;
                loggingIn = false;
                id = login.getId();
            } else if (login.getType() == LoginEvent.LOGIN_FAIL) {
                loggingIn = false;
                try {
                    cleanDisconnect();
                } catch (IOException ex) {

                }
            }
        }

        if (hasInterface()) ui.eventReceived(event);
    }

}
