package chat.common;

import java.io.Serializable;

/**
 * Created by Austin on 4/15/2017.
 */
public class Message extends Sendable {

    private Object contents;

    public Message(Serializable contents) {this.contents = contents;}

    public Message(Serializable source, Serializable contents) {
        super(source);
        this.contents = contents;
    }

    public Object getContents() {
        return contents;
    }

}
