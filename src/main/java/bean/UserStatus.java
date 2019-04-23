package bean;

public class UserStatus {
    private String name;
    private long timestamp;
    private String action;

    public UserStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public long getEpoch() {
        return this.timestamp;
    }

    public String getAction() {
        return this.action;
    }

    public void setStatus(long timestamp, String action) {
        this.timestamp = timestamp;
        this.action = action;
    }

    public String toString() {
        return "Last activity from " + this.getName() + " was " + this.getAction();
    }
}
