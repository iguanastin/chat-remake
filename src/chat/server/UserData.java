package chat.server;

import chat.common.Sendable;
import chat.common.events.DisconnectEvent;
import chat.common.events.Event;
import ocsf.server.ConnectionToClient;

import java.io.IOException;
import java.util.ArrayList;

public class UserData {

    //----------- Instance Variables -----------------------------------------------------------------------------------

    private String id;
    private String password;
    private ConnectionToClient client;
    private ArrayList<String> blocked;


    //------------- Constructors ---------------------------------------------------------------------------------------

    public UserData(String id, String password) {
        blocked = new ArrayList<>();

        setId(id);
        setPassword(password);
    }

    //------------- Functionality --------------------------------------------------------------------------------------

    public boolean send(Sendable message) {
        if (isLoggedIn()) {
            try {
                getClient().sendToClient(message);

                return true;
            } catch (IOException ex) {
                return false;
            }
        } else {
            return false;
        }
    }

    //--------- Getters/Setters ----------------------------------------------------------------------------------------

    public String getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public ConnectionToClient getClient() {
        return client;
    }

    public void setId(String id) {
        if (id.contains(" ")) {
            throw new IllegalArgumentException("Illegal space (' ') character");
        }

        this.id = id;
    }

    public void setPassword(String password) {
        if (id.contains(" ")) {
            throw new IllegalArgumentException("Illegal space (' ') character");
        }

        this.password = password;
    }

    public void setClient(ConnectionToClient client) {
        this.client = client;
    }

    public boolean isLoggedIn() {
        return getClient() != null;
    }

    public ArrayList<String> getBlocked() {
        return blocked;
    }

    public boolean isBlocking(String id) {
        return blocked.contains(id);
    }

    public boolean block(String id) {
        if (!isBlocking(id)) {
            blocked.add(id);
            return true;
        }

        return false;
    }

    public boolean unblock(String id) {
        if (isBlocking(id)) {
            blocked.remove(id);
            return true;
        }

        return false;
    }

    public void cleanDisconnect() {
        if (isLoggedIn()) {
            send(new DisconnectEvent());
            try {
                client.close();
            } catch (IOException ex) {

            }
        }
    }

}
