package chat.common.events;

/**
 * Created by Austin on 4/17/2017.
 */
public class LoginEvent extends Event {

    public static final int LOGIN_REQUEST = 0;
    public static final int LOGIN_FAIL = 1;
    public static final int LOGIN_SUCCEED = 2;


    private String id, password, message;
    private int type;

    public LoginEvent(String id, String password, int type) {
        this(id, password, type, null);
    }

    public LoginEvent(String id, String password, int type, String message) {
        set(id, password, type, message);
    }

    public void set(String id, String password, int type, String message) {
        this.id = id;
        this.password = password;
        this.type = type;
        this.message = message;
    }

    public String getPassword() {
        return password;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public int getType() {
        return type;
    }

}
