package Algorithms;

import Models.GanttBlock;
import Models.Process;
import Models.ProcessResult;
import Models.SchedulingResult;
import java.util.*;

public class RoundRobin {
    public static SchedulingResult schedule(List<Process> processes, int timeQuantum) {
        int n = processes.size();

        Queue<Process> queue = new LinkedList<>();
        List<GanttBlock> gantt = new ArrayList<>();
        List<ProcessResult> processResults = new ArrayList<>();

        // Arrays to track remaining times, waiting times, last queued times, start times
        int[] remainingTimes = new int[n];
        int[] lastQueuedTimes = new int[n];
        int[] startTimes = new int[n];
        boolean[] started = new boolean[n];
        int[] waitingTimes = new int[n]; // To store total waiting time for each process

        // Initialize remaining times and last queued times
        for (int i = 0; i < n; i++) {
            remainingTimes[i] = processes.get(i).getBurstTime();
            lastQueuedTimes[i] = processes.get(i).getArrivalTime();
            startTimes[i] = -1; // not started yet
            waitingTimes[i] = 0;
        }

        int currentTime = 0;
        double totalWaiting = 0;
        double totalTurnaround = 0;

        Set<Process> inQueueSet = new HashSet<>();

        while (true) {
            // Check if all processes are done
            boolean allDone = true;
            for (int rem : remainingTimes) {
                if (rem > 0) {
                    allDone = false;
                    break;
                }
            }
            if (allDone) break;

            // Add newly arrived processes at current time
            for (int i = 0; i < n; i++) {
                Process p = processes.get(i);
                if (p.getArrivalTime() == currentTime && remainingTimes[i] > 0 && !inQueueSet.contains(p)) {
                    queue.add(p);
                    inQueueSet.add(p);
                    lastQueuedTimes[i] = currentTime;
                }
            }

            if (queue.isEmpty()) {
                currentTime++;
                continue;
            }

            Process currentProcess = queue.poll();
            int index = processes.indexOf(currentProcess);
            int startTime = currentTime;
            if (!started[index]) {
                startTimes[index] = currentTime;
                started[index] = true;
            }

            int execTime = Math.min(remainingTimes[index], timeQuantum);
            int timeBeforeExecution = currentTime;
            currentTime += execTime;

            // Record in Gantt chart
            gantt.add(new GanttBlock(timeBeforeExecution, currentTime, "P" + currentProcess.getId()));

            // Update waiting time for the process
            int waitTimeIncrement = timeBeforeExecution - lastQueuedTimes[index];
            waitingTimes[index] += waitTimeIncrement;

            // Update last queued time
            lastQueuedTimes[index] = currentTime;

            // Check for new arrivals during execution
            for (int t = timeBeforeExecution; t < currentTime; t++) {
                for (int i = 0; i < n; i++) {
                    Process p = processes.get(i);
                    if (p.getArrivalTime() == t && remainingTimes[i] > 0 && !inQueueSet.contains(p)) {
                        queue.add(p);
                        inQueueSet.add(p);
                        lastQueuedTimes[i] = t;
                    }
                }
            }

            // Decrease remaining time
            remainingTimes[index] -= execTime;

            // If process not finished, re-queue
            if (remainingTimes[index] > 0) {
                queue.add(currentProcess);
                inQueueSet.add(currentProcess);
                lastQueuedTimes[index] = currentTime;
            } else {
                // Process finished
                int turnaroundTime = currentTime - processes.get(index).getArrivalTime();

                // Total waiting time = turnaround - burst time
                int burstTime = processes.get(index).getBurstTime();
                int waitingTime = turnaroundTime - burstTime;

                totalWaiting += waitingTime;
                totalTurnaround += turnaroundTime;

                processResults.add(new ProcessResult(
                        currentProcess.getId(),
                        currentProcess.getArrivalTime(),
                        burstTime,
                        startTimes[index],
                        currentTime
                ));
            }
        }

        double avgWaiting = totalWaiting / n;
        double avgTurnaround = totalTurnaround / n;

        return new SchedulingResult(avgWaiting, avgTurnaround, gantt, processResults);
    }

    // Overloaded method with default quantum
    public static SchedulingResult schedule(List<Process> processes) {
        return schedule(processes, 4);
    }
}