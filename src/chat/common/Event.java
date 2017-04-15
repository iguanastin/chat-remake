package chat.common;

/**
 * Created by Austin on 4/15/2017.
 */
public class Event extends Sendable {

    public static final int EVENT_DISCONNECT = 0;


    private final int type;

    public Event(Object source, int type) {
        super(source);

        this.type = type;
    }

    public int getType() {
        return type;
    }

}
