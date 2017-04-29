package chat.server;

import chat.common.Message;
import chat.common.events.Event;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by Austin on 4/26/2017.
 */
public class ConsoleServerIF implements ServerInterface {

    //-------------------- Static variables ----------------------------------------------------------------------------

    private static final char COMMAND_IDENTIFIER = '/';

    //------------- Instance variables ---------------------------------------------------------------------------------

    private ChatServer server;


    //---------------- Main --------------------------------------------------------------------------------------------

    public static void main(String[] args) {
        int port = 5555;

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {

            }
        }

        new ConsoleServerIF(port);
    }

    //------------ Constructors ----------------------------------------------------------------------------------------

    public ConsoleServerIF(int port) {
        server = new ChatServer(port);
        server.setInterface(this);

        System.out.println("Initialized server");
        System.err.println("Server is currently offline");

        receiveInput();
    }

    //------------- Handlers -------------------------------------------------------------------------------------------

    private void receiveInput() {
        Scanner scan = new Scanner(System.in);

        while (true) {
            String input = scan.nextLine();

            if (input.isEmpty()) continue;

            if (input.charAt(0) == COMMAND_IDENTIFIER) {
                handleCommand(input);
            } else {
                handleMessage(input);
            }
        }
    }

    private void handleMessage(String message) {
        Message msg = new Message(ChatServer.SERVER_ID, message);
        server.sendToAllClients(msg);
        messageReceived(msg);
    }

    private void handleCommand(String message) {
        message = message.substring(1); // Remove command delimiter

        String command = message;
        if (command.contains(" ")) {
            command = message.substring(0, message.indexOf(' ')).toLowerCase(); // If message contains a space character, command is only first word
        }

        String remaining = null;
        if (message.contains(" ")) {
            remaining = message.substring(message.indexOf(' ') + 1); // If message contains space character, remaining set to everything beyond space
        }

        if (command.equals("quit")) {
            handleQuitCommand();
        } else if (command.equals("stop")) {
            handleStopCommand();
        } else if (command.equals("start")) {
            handleStartCommand();
        } else if (command.equals("setport")) {
            handleSetPortCommand(remaining);
        } else if (command.equals("kick")) {
            handleKickCommand(remaining);
        } else {
            System.err.println("Unrecognized command: " + command);
        }
    }

    private void handleKickCommand(String remaining) {
        if (!server.isListening()) {
            System.err.println("Cannot kick users while server is stopped");
        } else if (remaining == null || remaining.isEmpty() || remaining.contains(" ")) {
            System.err.println("Invalid format. Expected: /kick [USER_ID]");
        } else {
            UserData data = server.getUserData(remaining);

            if (data == null) {
                System.err.println("No such user: " + remaining);
            } else if (!data.isLoggedIn()) {
                System.err.println("User is not logged in");
            } else {
                data.cleanDisconnect();

                System.err.println("Kicked user: " + remaining);
            }
        }
    }

    private void handleQuitCommand() {
        try {
            server.stop();
        } catch (IOException ex) {
            System.err.println("Failed to stop server: " + ex.getMessage());
        }

        System.err.println("Quitting...");

        System.exit(0);
    }

    private void handleStopCommand() {
        try {
            server.stop();

            System.err.println("Stopped the server");
        } catch (IOException ex) {
            System.err.println("Failed to stop server: " + ex.getMessage());
        }
    }

    private void handleStartCommand() {
        if (server.isListening()) {
            System.err.println("Server is already running");
        } else {
            try {
                server.start();

                System.err.println("Started the server successfully");
            } catch (IOException ex) {
                System.err.println("Failed to start the server: " + ex.getMessage());
            }
        }
    }

    private void handleSetPortCommand(String remaining) {
        if (server.isListening()) {
            System.err.println("Cannot change port while server is running");
        } else if (remaining == null || remaining.isEmpty() || remaining.contains(" ")) {
            System.err.println("Invalid format. Expected: /setport [INTEGER]");
        } else {
            try {
                int port = Integer.parseInt(remaining);

                if (port < 1025 || port > 65535) {
                    System.err.println("Invalid format. Expected number in range [1025, 65535]");
                } else {
                    server.setPort(port);

                    System.out.println("Set port to: " + port);
                }
            } catch (NumberFormatException ex) {
                System.err.println("Invalid format. Expected: /setport [INTEGER]");
            }
        }
    }

    @Override
    public void messageReceived(Message msg) {
        System.out.println(msg);
    }

    @Override
    public void eventReceived(Event event) {

    }

    @Override
    public void clientConnected(String ip) {
        System.err.println("Client connected: " + ip);
    }

    @Override
    public void clientDisconnected(String ip) {
        System.err.println("Client disconnected: " + ip);
    }

    @Override
    public void clientDisconnected(String ip, String id) {
        System.err.println("User disconnected: " + id + " (" + ip + ")");
    }

    @Override
    public void clientLoggedIn(String ip, String id) {
        System.err.println("Client logged in as: " + id + " (" + ip + ")");
    }

}
