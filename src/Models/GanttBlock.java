package Models;

public class GanttBlock {
    private int startTime;
    private int endTime;
    private String label;

    public GanttBlock(int startTime, int endTime, String label) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.label = label;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public String getLabel() {
        return label;
    }
}
