package chat.server;

import ocsf.server.ConnectionToClient;

public class UserData {

    //----------- Instance Variables -----------------------------------------------------------------------------------

    private String id;
    private String password;
    private ConnectionToClient client;


    //------------- Constructors ---------------------------------------------------------------------------------------

    public UserData(String id, String password) {
        setId(id);
        setPassword(password);
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
}
