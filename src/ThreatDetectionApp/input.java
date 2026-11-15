package ThreatDetectionApp;

public abstract class input {
    private int ID;
    private String Content;

    public input(int ID, String Content) {
        this.ID = ID;
        this.Content = Content;
    }

    public int getID() {
        return ID;
    }

    public String getContent() {
        return Content;
    }
    public void setContent(String Content) {
        this.Content = Content;
    }
}
