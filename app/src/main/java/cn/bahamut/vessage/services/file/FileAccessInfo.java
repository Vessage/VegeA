package cn.bahamut.vessage.services.file;

/**
 * Created by alexchow on 16/4/11.
 */
public class FileAccessInfo {
    public String fileId;
    public String server;
    public String bucket;
    public String expireAt;
    public String serverType;
    public String localPath;


    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(String expireAt) {
        this.expireAt = expireAt;
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public boolean isOnAliOSSServer() {
        return "alioss".equalsIgnoreCase(getServerType());
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

}
