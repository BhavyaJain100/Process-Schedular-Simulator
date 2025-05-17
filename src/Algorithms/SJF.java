package Algorithms;

import Models.GanttBlock;
import Models.Process;
import Models.ProcessResult;
import Models.SchedulingResult;
import java.util.*;

public class SJF {
    public static SchedulingResult schedule(List<Process> processes) {
        List<Process> remaining = new ArrayList<>(processes);
        List<GanttBlock> gantt = new ArrayList<>();
        List<ProcessResult> processResults = new ArrayList<>();
        int currentTime = 0;
        double totalWaiting = 0;
        double totalTurnaround = 0;
        
        while(!remaining.isEmpty()) {
            Process next = getNextProcess(remaining, currentTime);
            if(next == null) {
                currentTime++;
                continue;
            }
            
            int startTime = currentTime;
            int endTime = currentTime + next.getBurstTime();
            int waiting = startTime - next.getArrivalTime();
            int turnaround = endTime - next.getArrivalTime();
            
            // Record process execution details
            processResults.add(new ProcessResult(
                next.getId(),
                next.getArrivalTime(),
                next.getBurstTime(),
                startTime,
                endTime
            ));
            
            totalWaiting += waiting;
            totalTurnaround += turnaround;
            
            gantt.add(new GanttBlock(startTime, endTime, "P" + next.getId()));
            currentTime = endTime;
            remaining.remove(next);
        }
        
        double avgWaiting = totalWaiting / processes.size();
        double avgTurnaround = totalTurnaround / processes.size();
        return new SchedulingResult(avgWaiting, avgTurnaround, gantt, processResults);
    }

    private static Process getNextProcess(List<Process> processes, int currentTime) {
        return processes.stream()
            .filter(p -> p.getArrivalTime() <= currentTime)
            .min(Comparator.comparingInt(Process::getBurstTime))
            .orElse(null);
    }
}