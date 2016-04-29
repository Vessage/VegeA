package cn.bahamut.chicago;

/**
 * Created by alexchow on 16/4/29.
 */
public class ClientReceiveMessageEventArgs {
    private byte[] receiveMessage;

    public byte[] getReceiveMessage() {
        return receiveMessage;
    }

    public void setReceiveMessage(byte[] receiveMessage) {
        this.receiveMessage = receiveMessage;
    }
}
