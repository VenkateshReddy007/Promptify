public class Notification {
    private int notificationId;
    private int analysisId;
    private String message;
    private String status;
    private String sentTime;

    public Notification() {
    }

    public Notification(int notificationId, int analysisId, String message, String status, String sentTime) {
        this.notificationId = notificationId;
        this.analysisId = analysisId;
        this.message = message;
        this.status = status;
        this.sentTime = sentTime;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public int getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(int analysisId) {
        this.analysisId = analysisId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSentTime() {
        return sentTime;
    }

    public void setSentTime(String sentTime) {
        this.sentTime = sentTime;
    }
}
