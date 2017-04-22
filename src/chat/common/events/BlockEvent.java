package chat.common.events;

/**
 * Created by Austin on 4/22/2017.
 */
public class BlockEvent extends Event {

    public static final int BLOCK = 0;
    public static final int UNBLOCK = 1;
    public static final int BLOCK_FAIL = 2;
    public static final int BLOCK_SUCCEED = 3;
    public static final int UNBLOCK_FAIL = 4;
    public static final int UNBLOCK_SUCCEED = 5;


    private String target;
    private String message;
    private int type;

    public BlockEvent(String target, int type) {
        this(target, type, null);
    }

    public BlockEvent(String target, int type, String message) {
        this.target = target;
        this.type = type;
        this.message = message;
    }

    public String getTarget() {
        return target;
    }

    public int getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

}
