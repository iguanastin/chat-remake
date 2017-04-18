package chat.common.events;

/**
 * Created by Austin on 4/17/2017.
 */
public class LoginFailedEvent extends Event {

    private String message;

    public LoginFailedEvent(String message) {
        this.message = message;
    }

    public LoginFailedEvent() {
    }

    public String getMessage() {
        return message;
    }

}
