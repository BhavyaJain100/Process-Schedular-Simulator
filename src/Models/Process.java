package Models;

public class Process {
    private int id;
    private int arrivalTime;
    private int burstTime;
    private int priority;

    public Process(int id, int arrival, int burst, int priority) {
        this.id = id;
        this.arrivalTime = arrival;
        this.burstTime = burst;
        this.priority = priority;
    }

    public int getId() {
        return id;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public int getPriority() {
        return priority;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public void setBurstTime(int burstTime) {
        this.burstTime = burstTime;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
