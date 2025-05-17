package Algorithms;

import Models.GanttBlock;
import Models.Process;
import Models.ProcessResult;
import Models.SchedulingResult;
import java.util.*;

public class FCFS {
    public static SchedulingResult schedule(List<Process> processes) {
        List<Process> sorted = new ArrayList<>(processes);
        sorted.sort(Comparator.comparingInt(p -> p.getArrivalTime()));
        
        int currentTime = 0;
        double totalWaiting = 0;
        double totalTurnaround = 0;
        List<GanttBlock> gantt = new ArrayList<>();
        List<ProcessResult> processResults = new ArrayList<>();

        for(Process p : sorted) {
            if(currentTime < p.getArrivalTime()) {
                currentTime = p.getArrivalTime();
            }
            
            int startTime = currentTime;
            int endTime = currentTime + p.getBurstTime();
            int waiting = startTime - p.getArrivalTime();
            int turnaround = endTime - p.getArrivalTime();
            
            totalWaiting += waiting;
            totalTurnaround += turnaround;
            
            // Record process execution details
            processResults.add(new ProcessResult(
                p.getId(),
                p.getArrivalTime(),
                p.getBurstTime(),
                startTime,
                endTime
            ));
            
            gantt.add(new GanttBlock(startTime, endTime, "P" + p.getId()));
            currentTime = endTime;
        }
        
        double avgWaiting = totalWaiting / processes.size();
        double avgTurnaround = totalTurnaround / processes.size();
        
        return new SchedulingResult(avgWaiting, avgTurnaround, gantt, processResults);
    }
}