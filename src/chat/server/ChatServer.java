package chat.server;

import chat.common.events.*;
import chat.common.Message;
import chat.common.Sendable;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class ChatServer extends AbstractServer {

    //------------- Static Variables -----------------------------------------------------------------------------------

    private static final String SERVER_ID = "server";

    private static final String USERDATA_FILEPATH = "userdata";

    //----------------- Instance Variables -----------------------------------------------------------------------------

    private ArrayList<UserData> users = new ArrayList<>();


    //--------------- Main ---------------------------------------------------------------------------------------------

    public static void main(String[] args) {
        try {
            System.out.println("Initializing...");

            new ChatServer(Integer.parseInt(args[0])).start();

            System.out.println("Listening...");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //--------------- Constructors -------------------------------------------------------------------------------------

    public ChatServer(int port) {
        super(port);

        loadUserdata();
    }

    //----------------- Methods ----------------------------------------------------------------------------------------

    private void loadUserdata() {
        try {
            Scanner scan = new Scanner(new File(USERDATA_FILEPATH));

            while (scan.hasNextLine()) {
                String[] split = scan.nextLine().split(" ");
                if (split.length == 0 || split.length > 2) {
                    System.err.println("Invalid format, expected: [ID] [PASSWORD]");
                }

                try {
                    users.add(new UserData(split[0], split[1]));
                } catch (IllegalArgumentException ex) {
                    System.err.println(ex.getMessage() + ": " + split[0] + " " + split[1]);
                }
            }

            scan.close();
        } catch (FileNotFoundException ex) {
            System.err.println("No such userdata file: " + new File(USERDATA_FILEPATH).getAbsolutePath());
        }
    }

    private void saveUserData() {
        try {
            PrintWriter writer = new PrintWriter(new File(USERDATA_FILEPATH));

            for (UserData user : users) {
                writer.println(user.getId() + " " + user.getPassword());
            }

            writer.close();
        } catch (FileNotFoundException ex) {
            System.err.println("Unable to save userdata to file: " + new File(USERDATA_FILEPATH).getAbsolutePath());
        }
    }

    public void start() throws IOException {
        listen();
    }

    public void stop() throws IOException {
        saveUserData();

        for (Thread thread : getClientConnections()) {
            ConnectionToClient client = (ConnectionToClient) thread;
            cleanDisconnectClient(client);
        }

        stopListening();
    }

    public UserData getUserData(String id) {
        for (UserData user : users) {
            if (user.getId().equals(id)) return user;
        }

        return null;
    }

    public UserData getUserData(ConnectionToClient client) {
        for (UserData user : users) {
            if (user.getClient() == client) return user;
        }

        return null;
    }

    //--------------- Handlers -----------------------------------------------------------------------------------------

    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        if (msg instanceof Sendable) {
            if (msg instanceof Event) {
                handleEventFromClient((Event) msg, client);
            } else if (msg instanceof Message) {
                handleActualMessageFromClient(client, (Message) msg);
            }
        } else {
            System.err.println("Received non-Sendable object: " + msg);
        }
    }

    private void handleActualMessageFromClient(ConnectionToClient client, Message msg) {
        sendToAllClients(msg);
    }

    private void handleEventFromClient(Event event, ConnectionToClient client) {
        if (event instanceof DisconnectEvent) {
            handleEventDisconnect(client);
        } else if (event instanceof LoginRequestEvent) {
            handleEventLoginRequest(event, client);
        }
    }

    //-------------- Event handling ------------------------------------------------------------------------------------

    private void handleEventDisconnect(ConnectionToClient client) {
        try {
            client.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void handleEventLoginRequest(Event event, ConnectionToClient client) {
        LoginRequestEvent login = (LoginRequestEvent) event;
        UserData user = getUserData(login.getId());

        if (user == null) {
            try {
                client.sendToClient(new LoginFailedEvent("User with ID \"" + login.getId() + "\" does not exist"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else if (!user.getPassword().equals(login.getPassword())) {
            try {
                client.sendToClient(new LoginFailedEvent("Incorrect password"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            try {
                client.sendToClient(new LoginSuccessEvent(login.getId()));
                sendToAllClients(new UserConnectedEvent(login.getId()));
                user.setClient(client);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void cleanDisconnectClient(ConnectionToClient client) throws IOException {
        client.sendToClient(new DisconnectEvent());
        client.close();
    }

    //--------------- Callbacks ----------------------------------------------------------------------------------------

    @Override
    protected void clientConnected(ConnectionToClient client) {
        System.out.println("Client Connected: " + client);
    }

    @Override
    protected synchronized void clientDisconnected(ConnectionToClient client) {
        System.out.println("Client Disconnected: " + client);

        UserData user = getUserData(client);
        if (user != null) {
            user.setClient(null);
            sendToAllClients(new UserDisconnectedEvent(user.getId()));
        }
    }

}
