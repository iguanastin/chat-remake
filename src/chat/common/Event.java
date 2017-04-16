package chat.common;

import java.io.Serializable;

/**
 * Created by Austin on 4/15/2017.
 */
public class Event extends Sendable {

    public static final int EVENT_DISCONNECT = 0;
    public static final int EVENT_LOGIN = 1;


    private int type;
    private Serializable[] data;

    public Event(int type) {
        this.type = type;
    }

    public Event(int type, Serializable[] data) {
        this(type);
        this.data = data;
    }

    public Event(Serializable source, int type) {
        super(source);

        this.type = type;
    }

    public Event(Serializable source, int type, Serializable[] data) {
        this(source, type);
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public Serializable[] getData() {
        return data;
    }
}
