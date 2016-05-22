package cn.bahamut.vessage.services.activities;

/**
 * Created by alexchow on 16/5/16.
 */
public class BoardData {
    private String id;
    private int badge = 0;
    private boolean miniBadge = false;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getBadge() {
        return badge;
    }

    public void setBadge(int badge) {
        this.badge = badge;
    }

    public boolean isMiniBadge() {
        return miniBadge;
    }

    public void setMiniBadge(boolean miniBadge) {
        this.miniBadge = miniBadge;
    }
}
