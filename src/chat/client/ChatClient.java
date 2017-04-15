package chat.client;

import chat.common.Event;
import chat.common.Sendable;
import ocsf.client.AbstractClient;

import java.io.IOException;

public class ChatClient extends AbstractClient {

    private String id = "";


    public ChatClient(String host, int port) {
        super(host, port);
    }

    public static void main(String[] args) {
        try {
            System.out.println("Initializing...");
            System.out.println("Connecting...");

            ChatClient client = new ChatClient(args[0], Integer.parseInt(args[1]));

            client.connect();
            client.sendAndDisconnect();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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

    private void connect() throws IOException {
        openConnection();
    }

    //-------------- Handlers ------------------------------------------------------------------------------------------

    @Override
    protected void handleMessageFromServer(Object msg) {
        if (msg instanceof Sendable) {
            if (msg instanceof Event) {
                handleEventFromServer((Event) msg);
            }
        } else {
            System.err.println("Received non-Sendable object: " + msg);
        }
    }

    private void handleEventFromServer(Event event) {
        if (event.getType() == Event.EVENT_DISCONNECT) {
            try {
                disconnect();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    //----------------- Callbacks --------------------------------------------------------------------------------------

    @Override
    protected void connectionEstablished() {
        System.out.println("Connected to " + getHost() + ":" + getPort());
    }

    @Override
    protected void connectionClosed() {
        System.out.println("Disconnected from " + getHost() + ":" + getPort());
    }
}
