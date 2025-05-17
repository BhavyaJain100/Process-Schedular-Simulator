package Models;

public class ProcessResult {
    private int pid;
    private int arrivalTime;
    private int burstTime;
    private int startTime;
    private int endTime;

    public ProcessResult(int pid, int arrivalTime, int burstTime, int startTime, int endTime) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getWaitingTime() { return startTime - arrivalTime; }
    public int getTurnaroundTime() { return endTime - arrivalTime; }
    
    // Getters
    public int getPid() { return pid; }
    public int getArrivalTime() { return arrivalTime; }
    public int getBurstTime() { return burstTime; }
    public int getStartTime() { return startTime; }
    public int getEndTime() { return endTime; }
}