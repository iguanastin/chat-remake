package chat.common.events;

/**
 * Created by Austin on 4/17/2017.
 */
public class LoginSuccessEvent extends Event {

    private String id;

    public LoginSuccessEvent(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

}
