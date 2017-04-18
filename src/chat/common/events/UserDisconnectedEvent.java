package chat.common.events;

/**
 * Created by Austin on 4/17/2017.
 */
public class UserDisconnectedEvent extends Event {

    private String id;

    public UserDisconnectedEvent(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

}
