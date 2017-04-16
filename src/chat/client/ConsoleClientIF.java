package chat.client;

import chat.common.Event;
import chat.common.Message;

/**
 * Created by Austin on 4/16/2017.
 */
public class ConsoleClientIF implements ClientInterface {

    private ChatClient client;


    public static void main(String[] args) {
        ConsoleClientIF console = new ConsoleClientIF(args[0], Integer.parseInt(args[1]));

    }

    public ConsoleClientIF() {
        client = new ChatClient();
    }

    public ConsoleClientIF(String host, int port) {
        client = new ChatClient(host, port);
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
    }

}
