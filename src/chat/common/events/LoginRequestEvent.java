package chat.common.events;

/**
 * Created by Austin on 4/17/2017.
 */
public class LoginRequestEvent extends Event {

    private String id, password;

    public LoginRequestEvent(String id, String password) {
        this.id = id;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getId() {
        return id;
    }

}
