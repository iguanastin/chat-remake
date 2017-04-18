package chat.common;

import java.io.Serializable;


public class PrivateMessage extends Message {

    private Object destination;

    public PrivateMessage(Serializable destination, Serializable contents) {
        super(contents);
        this.destination = destination;
    }

    public Object getDestination() {
        return destination;
    }

}
