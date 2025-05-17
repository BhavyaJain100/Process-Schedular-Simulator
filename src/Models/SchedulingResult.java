package Models;

import java.util.List;

public class SchedulingResult {
    private double averageWaitingTime;
    private double averageTurnaroundTime;
    private List<GanttBlock> ganttChart;
    private List<ProcessResult> processResults;

    public SchedulingResult(double avgWaiting, double avgTurnaround, 
                           List<GanttBlock> gantt, List<ProcessResult> results) {
        this.averageWaitingTime = avgWaiting;
        this.averageTurnaroundTime = avgTurnaround;
        this.ganttChart = gantt;
        this.processResults = results;
    }

    // Getters
    public double getAverageWaitingTime() { return averageWaitingTime; }
    public double getAverageTurnaroundTime() { return averageTurnaroundTime; }
    public List<GanttBlock> getGanttChart() { return ganttChart; }
    public List<ProcessResult> getProcessResults() { return processResults; }
}