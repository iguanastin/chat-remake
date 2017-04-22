package chat.common.events;

/**
 * Created by Austin on 4/18/2017.
 */
public class PrivateMessageFailEvent extends Event {

    private String message;

    public PrivateMessageFailEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
