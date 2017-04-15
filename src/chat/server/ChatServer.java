package chat.server;

import chat.common.Event;
import chat.common.Message;
import chat.common.Sendable;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

import java.io.IOException;

public class ChatServer extends AbstractServer {

    private static final String SERVER_ID = "server";


    public ChatServer(int port) {
        super(port);
    }

    public static void main(String[] args) {
        try {
            System.out.println("Initializing...");

            new ChatServer(Integer.parseInt(args[0])).listen();

            System.out.println("Listening...");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //----------------- Functionality ----------------------------------------------------------------------------------

    public void start() throws IOException {
        listen();
    }

    public void stop() throws IOException {
        for (Thread thread : getClientConnections()) {
            ConnectionToClient client = (ConnectionToClient) thread;
            client.sendToClient(new Event(SERVER_ID, Event.EVENT_DISCONNECT));
            client.close();
        }
    }

    //--------------- Handlers -----------------------------------------------------------------------------------------

    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        if (msg instanceof Sendable) {
            if (msg instanceof Event) {
                handleEventFromClient((Event) msg, client);
            } else if (msg instanceof Message) {

            }
        } else {
            System.err.println("Received non-Sendable object: " + msg);
        }
    }

    private void handleEventFromClient(Event event, ConnectionToClient client) {
        if (event.getType() == Event.EVENT_DISCONNECT) {
            try {
                client.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    //--------------- Callbacks ----------------------------------------------------------------------------------------

    @Override
    protected void clientConnected(ConnectionToClient client) {
        System.out.println("Client Connected: " + client);
    }

    @Override
    protected synchronized void clientDisconnected(ConnectionToClient client) {
        System.out.println("Client Disconnected: " + client);
    }

}
