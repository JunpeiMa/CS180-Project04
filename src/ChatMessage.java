
import java.io.Serializable;

final class ChatMessage implements Serializable {

    private static final long serialVersionUID = 6898543889087L;

    private String msg;
    private int type;
    private String recipient;

    public ChatMessage(String msg, int type, String recipient){
        this.msg = msg;
        this.type = type;
        this.recipient = recipient;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getMsg() {
        return msg;
    }

    public int getType() {
        return type;
    }

    public String getRecipient() {
        return recipient;
    }

}
