package cn.bahamut.chicago;

/**
 * Created by alexchow on 16/4/29.
 */
public class ClientBaseMessage {
    private String extension;
    private int commandId;
    private String commandName;

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public int getCommandId() {
        return commandId;
    }

    public void setCommandId(int commandId) {
        this.commandId = commandId;
    }

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }
}
