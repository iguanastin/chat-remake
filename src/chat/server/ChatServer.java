package chat.server;

import chat.common.PrivateMessage;
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

    public static final String SERVER_ID = "server";

    private static final String USERDATA_FILEPATH = "userdata";

    //----------------- Instance Variables -----------------------------------------------------------------------------

    private ArrayList<UserData> users = new ArrayList<>();

    private ServerInterface ui;


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

    public void setInterface(ServerInterface ui) {
        this.ui = ui;
    }

    public boolean hasInterface() {
        return ui != null;
    }

    public void loadUserdata() {
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

    public void saveUserData() {
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

        cleanDisconnectAllClients();

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

    private void cleanDisconnectClient(ConnectionToClient client) throws IOException {
        client.sendToClient(new DisconnectEvent());
        client.close();
    }

    private boolean isUserLoggedIn(ConnectionToClient client) {
        return getUserData(client) != null;
    }

    public boolean isUserLoggedIn(String id) {
        return getUserData(id) != null;
    }

    public boolean userExists(String id) {
        return getUserData(id) != null;
    }

    public void cleanDisconnectAllClients() {
        if (isListening()) {
            for (Thread thread : getClientConnections()) {
                ConnectionToClient client = (ConnectionToClient) thread;

                try {
                    cleanDisconnectClient(client);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
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
        if (!isUserLoggedIn(client)) {
            try {
                cleanDisconnectClient(client);
            } catch (IOException ex) {
            }
        }

        UserData source = getUserData(client);
        msg.setSource(source.getId());

        if (msg instanceof PrivateMessage) {
            handlePrivateMessageFromClient(source, (PrivateMessage) msg);
        } else {
            handleGlobalMessageFromClient(source, msg);
        }
    }

    private void handleGlobalMessageFromClient(UserData source, Message msg) {
        for (UserData user : users) {
            if (user.isLoggedIn() && !user.isBlocking(source.getId())) {
                user.send(msg);
            }
        }

        if (hasInterface()) ui.messageReceived(msg);
    }

    private void handlePrivateMessageFromClient(UserData source, PrivateMessage msg) {
        UserData destination = getUserData((String) msg.getDestination());
        if (destination == null) {
            source.send(new PrivateMessageFailEvent("No such user: " + msg.getDestination()));
        } else if (destination.isBlocking(source.getId())) {
            source.send(new PrivateMessageFailEvent("Target user is blocking you"));
        } else if (destination.isLoggedIn()) {
            destination.send(msg);

            if (hasInterface()) ui.messageReceived(msg);
        } else {
            source.send(new PrivateMessageFailEvent("Target user is offline"));
        }
    }

    private void handleEventFromClient(Event event, ConnectionToClient client) {
        UserData user = getUserData(client);

        if (user != null) {
            if (event instanceof BlockEvent) {
                BlockEvent block = (BlockEvent) event;

                if (block.getType() == BlockEvent.BLOCK) {
                    handleEventBlock(block, user);
                } else if (block.getType() == BlockEvent.UNBLOCK) {
                    handleEventUnblock(block, user);
                }
            }
        } else {
            if (event instanceof LoginEvent) {
                handleEventLogin(event, client);
            } else if (event instanceof DisconnectEvent) {
                handleEventDisconnect(client);
            }
        }

        if (hasInterface()) ui.eventReceived(event);
    }

    //-------------- Event handling ------------------------------------------------------------------------------------

    private void handleEventDisconnect(ConnectionToClient client) {
        try {
            client.close();
            clientDisconnected(client);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void handleEventLogin(Event event, ConnectionToClient client) {
        LoginEvent login = (LoginEvent) event;
        UserData user = getUserData(login.getId());

        if (user == null) {
            try {
                client.sendToClient(new LoginEvent(login.getId(), null, LoginEvent.LOGIN_FAIL, "No such ID"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else if (user.isLoggedIn()) {
            try {
                client.sendToClient(new LoginEvent(login.getId(), null, LoginEvent.LOGIN_FAIL, "User with that name is already logged in"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else if (!user.getPassword().equals(login.getPassword())) {
            try {
                client.sendToClient(new LoginEvent(login.getId(), null, LoginEvent.LOGIN_FAIL, "Incorrect password"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            try {
                client.sendToClient(new LoginEvent(login.getId(), null, LoginEvent.LOGIN_SUCCEED));
                sendToAllClients(new UserConnectedEvent(login.getId()));
                user.setClient(client);

                if (hasInterface()) ui.clientLoggedIn((String) client.getInfo("ip"), user.getId());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void handleEventBlock(BlockEvent event, UserData user) {
        if (!userExists(event.getTarget())) {
            user.send(new BlockEvent(event.getTarget(), BlockEvent.BLOCK_FAIL, "No such user"));
        } else if (event.getTarget().equals(user.getId())) {
            user.send(new BlockEvent(event.getTarget(), BlockEvent.BLOCK_FAIL, "Cannot block yourself"));
        } else if (user.isBlocking(event.getTarget())) {
            user.send(new BlockEvent(event.getTarget(), BlockEvent.BLOCK_FAIL, "Already blocking user"));
        } else {
            user.block(event.getTarget());

            user.send(new BlockEvent(event.getTarget(), BlockEvent.BLOCK_SUCCEED));
        }
    }

    private void handleEventUnblock(BlockEvent event, UserData user) {
        if (!userExists(event.getTarget())) {
            user.send(new BlockEvent(event.getTarget(), BlockEvent.UNBLOCK_FAIL, "No such user"));
        } else if (event.getTarget().equals(user.getId())) {
            user.send(new BlockEvent(event.getTarget(), BlockEvent.UNBLOCK_FAIL, "Cannot unblock yourself"));
        } else if (!user.isBlocking(event.getTarget())) {
            user.send(new BlockEvent(event.getTarget(), BlockEvent.UNBLOCK_FAIL, "User is already unblocked"));
        } else {
            user.unblock(event.getTarget());

            user.send(new BlockEvent(event.getTarget(), BlockEvent.UNBLOCK_SUCCEED));
        }
    }

    //--------------- Callbacks ----------------------------------------------------------------------------------------

    @Override
    protected void clientConnected(ConnectionToClient client) {
        client.setInfo("ip", client.getInetAddress().getHostAddress());

        if (hasInterface()) ui.clientConnected(client.getInetAddress().getHostAddress());
    }

    @Override
    protected synchronized void clientDisconnected(ConnectionToClient client) {
        UserData user = getUserData(client);
        if (user != null) {
            user.setClient(null);
            sendToAllClients(new UserDisconnectedEvent(user.getId()));

            if (hasInterface()) ui.clientDisconnected((String) client.getInfo("ip"), user.getId());
        } else {
            if (hasInterface()) ui.clientDisconnected((String) client.getInfo("ip"));
        }
    }

}
