
import java.io.Serializable;

final class ChatMessage implements Serializable {

    private static final long serialVersionUID = 6898543889087L;

    private String msg;
    private int type;

    public ChatMessage(String msg, int type){
        this.msg = msg;
        this.type = type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public int getType() {
        return type;
    }

}
