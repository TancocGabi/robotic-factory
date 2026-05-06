package ro.tuiasi.ac.robotic_factory;

public class RobotEvent {
    private String robotId;
    private String eventType;
    private String taskName;
    private long timestamp;
    private String details;

    public RobotEvent() {}

    public RobotEvent(String robotId, String eventType, String taskName, String details) {
        this.robotId = robotId;
        this.eventType = eventType;
        this.taskName = taskName;
        this.details = details;
        this.timestamp = System.currentTimeMillis();
    }

    public String getRobotId() { return robotId; }
    public void setRobotId(String robotId) { this.robotId = robotId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}