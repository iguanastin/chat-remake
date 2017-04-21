package chat.client;

import chat.common.PrivateMessage;
import chat.common.events.*;
import chat.common.Message;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by Austin on 4/16/2017.
 */
public class ConsoleClientIF implements ClientInterface {

    //------------ Static Variables ------------------------------------------------------------------------------------

    private static final char COMMAND_IDENTIFIER = '/';

    //------------ Instance Variables ----------------------------------------------------------------------------------

    private ChatClient client;


    //------------ Main ------------------------------------------------------------------------------------------------

    public static void main(String[] args) {
        ConsoleClientIF console = new ConsoleClientIF(args[0], Integer.parseInt(args[1]));
    }

    //----------- Constructors -----------------------------------------------------------------------------------------

    public ConsoleClientIF() {
        client = new ChatClient();
        client.setInterface(this);

        receiveInput();
    }

    public ConsoleClientIF(String host, int port) {
        client = new ChatClient(host, port);
        client.setInterface(this);

        receiveInput();
    }

    //-------------- Handlers ------------------------------------------------------------------------------------------

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
            commandQuit();
        } else if (command.equals("disconnect") || command.equals("dc")) {
            commandDisconnect();
        } else if (command.equals("connect")) {
            commandConnect(remaining);
        } else if (command.equals("w") || command.equals("whisper") || command.equals("pm")) {
            commandPrivateMessage(remaining);
        } else if (command.equals("sethost")) {
            commandSetHost(remaining);
        } else if (command.equals("setport")) {
            commandSetPort(remaining);
        } else {
            System.err.println("No known command \"" + command + "\"");
        }
    }

    private void handleMessage(String message) {
        if (!client.isConnected()) {
            System.out.println("Not currently connected to any server. Message not sent.");
            return;
        }

        try {
            client.sendToServer(new Message(client.getId(), message));
        } catch (IOException ex) {
            System.out.println("[Error sending message to server]");
        }
    }

    @Override
    public void messageReceived(Message message) {
        System.out.println(message);
    }

    @Override
    public void eventReceived(Event event) {
        if (event instanceof DisconnectEvent) {
            System.err.println("[Disconnected]");
        } else if (event instanceof LoginSuccessEvent) {
            System.err.println("[Logged in successfully as: " + ((LoginSuccessEvent) event).getId() + "]");
        } else if (event instanceof LoginFailedEvent) {
            System.err.println("[Failed to log in (" + ((LoginFailedEvent) event).getMessage() + ")]");
        } else if (event instanceof UserConnectedEvent) {
            System.err.println("[User joined: " + ((UserConnectedEvent) event).getId() + "]");
        } else if (event instanceof UserDisconnectedEvent) {
            System.err.println("[User left: " + ((UserDisconnectedEvent) event).getId() + "]");
        } else if (event instanceof PrivateMessageFailEvent) {
            System.err.println("[Failed to send private message: " + ((PrivateMessageFailEvent) event).getMessage() + "]");
        }
    }

    //------------ Commands --------------------------------------------------------------------------------------------

    private void commandConnect(String remaining) {
        if (client.isLoggedIn()) {
            System.err.println("Already connected");
        } else if (remaining == null || remaining.isEmpty()) {
            System.err.println("Missing username and password");
        } else if (!remaining.contains(" ") || remaining.split(" ").length > 2) {
            System.err.println("Invalid format. Expected: /connect [USERNAME] [PASSWORD]");
        } else {
            String[] split = remaining.split(" ");
            try {
                client.connect(split[0], split[1]);
            } catch (IOException ex) {
                System.err.println("[Error Sending Login Token]");
            }
        }
    }

    private void commandDisconnect() {
        if (!client.isConnected()) {
            System.err.println("Cannot disconnect while not connected");
        } else {
            try {
                client.cleanDisconnect();
                client.handleMessageFromServer(new DisconnectEvent());
            } catch (IOException ex) {
                System.err.println("[Error Disconnecting]");
            }
        }
    }

    private void commandQuit() {
        try {
            client.cleanDisconnect();
        } catch (IOException ex) {
            //Ignore failed disconnect
        }

        System.exit(0);
    }

    private void commandPrivateMessage(String remaining) {
        if (!client.isLoggedIn()) {
            System.err.println("Cannot send private messages while disconnected");
        } else if (remaining == null || remaining.isEmpty() || !remaining.contains(" ")) {
            System.err.println("Invalid format, expected: /w [USER] [MESSAGE]");
        } else {
            String user = remaining.substring(0, remaining.indexOf(' '));
            String message = remaining.substring(remaining.indexOf(' ') + 1);

            if (user.equalsIgnoreCase(client.getId())) {
                System.err.println("Cannot send private messages to yourself");
            } else {
                try {
                    client.sendToServer(new PrivateMessage(user, message));
                } catch (IOException ex) {
                    System.err.println("Unable to deliver private message");
                }
            }
        }
    }

    private void commandSetHost(String remaining) {
        if (client.isConnected()) {
            System.err.println("Cannot change host while connected");
        } else if (remaining == null || remaining.isEmpty() || remaining.contains(" ")) {
            System.err.println("Invalid format. Expected: /sethost [HOSTNAME/IP]");
        } else {
            client.setHost(remaining);

            System.err.println("Changed host to: " + remaining);
        }
    }

    private void commandSetPort(String remaining) {
        if (client.isConnected()) {
            System.err.println("Cannot change port while connected");
        } else if (remaining == null || remaining.isEmpty() || remaining.contains(" ")) {
            System.err.println("Invalid format. Expected: /setport [PORT]");
        } else {
            try {
                int port = Integer.parseInt(remaining);

                if (port <= 1024 || port > 65536) {
                    System.err.println("Invalid value. Expected value in range (1024-65536]");
                } else {
                    client.setPort(port);

                    System.err.println("Set port to: " + port);
                }
            } catch (NumberFormatException ex) {
                System.err.println("Invalid format. Port must be an integer");
            }
        }
    }

}
