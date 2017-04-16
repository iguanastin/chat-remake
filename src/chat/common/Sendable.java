package chat.common;

import java.io.Serializable;

/**
 * Created by Austin on 4/15/2017.
 */
public abstract class Sendable implements Serializable {

    private Object source;

    public Sendable(Serializable source) {
        this.source = source;
    }

    public Object getSource() {
        return source;
    }

}
