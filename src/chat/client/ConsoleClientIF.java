package chat.client;

import chat.common.Event;
import chat.common.Message;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by Austin on 4/16/2017.
 */
public class ConsoleClientIF implements ClientInterface {

    //------------ Static Variables ------------------------------------------------------------------------------------

    private static final char COMMAND_DELIMITER = '/';

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

            if (input.charAt(0) == COMMAND_DELIMITER) {
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
        }
    }

    private void handleMessage(String message) {
        if (!client.isConnected()) {
            System.out.println("Not currently connected to any server. Message not sent.");
            return;
        }

        try {
            client.sendToServer(new Message(message));
        } catch (IOException ex) {
            System.out.println("[Error sending message to server]");
        }
    }

    @Override
    public void messageReceived(Message message) {
        System.out.println("[" + message.getSource() + "]: " + message.getContents());
    }

    @Override
    public void eventReceived(Event event) {
        if (event.getType() == Event.EVENT_DISCONNECT) {
            System.out.println("[Disconnected]");
        }
        if (event.getType() == Event.EVENT_LOGIN_SUCCESS) {
            System.out.println("[Logged in successfully!]");
        }
        if (event.getType() == Event.EVENT_LOGIN_FAIL) {
            System.err.println("[Failed to log in: " + event.getData()[0] + "]");
        }
    }

    //------------ Commands --------------------------------------------------------------------------------------------

    private void commandConnect(String remaining) {
        if (remaining == null || remaining.isEmpty()) {
            System.out.println("Missing username and password");
        } else if (!remaining.contains(" ") || remaining.split(" ").length > 2) {
            System.out.println("Invalid format. Expected: /connect [USERNAME] [PASSWORD]");
        } else {
            String[] split = remaining.split(" ");
            try {
                client.connect(split[0], split[1]);
            } catch (IOException ex) {
                System.out.println("[Error Sending Login Token]");
            }
        }
    }

    private void commandDisconnect() {
        if (!client.isConnected()) {
            System.out.println("Cannot disconnect while not connected");
        } else {
            try {
                client.cleanDisconnect();
            } catch (IOException ex) {
                System.out.println("[Error Disconnecting]");
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

}
