package Utils;

import Algorithms.FCFS;
import Algorithms.SJF;
import Algorithms.PriorityScheduling;
import Algorithms.RoundRobin;
import Models.Process;
import Models.SchedulingResult;

import java.util.List;

public class AlgorithmSelector {

    public static String selectBestAlgorithm(List<Process> processes) {
        double bestScore = Double.MAX_VALUE;
        String bestAlgorithm = "";

        // Run FCFS
        SchedulingResult fcfsResult = FCFS.schedule(processes);
        if (fcfsResult.getAverageWaitingTime() < bestScore) {
            bestScore = fcfsResult.getAverageWaitingTime();
            bestAlgorithm = "FCFS";
        }

        // Run SJF
        SchedulingResult sjfResult = SJF.schedule(processes);
        if (sjfResult.getAverageWaitingTime() < bestScore) {
            bestScore = sjfResult.getAverageWaitingTime();
            bestAlgorithm = "SJF";
        }

        // Run Priority
        SchedulingResult prioResult = PriorityScheduling.schedule(processes);
        if (prioResult.getAverageWaitingTime() < bestScore) {
            bestScore = prioResult.getAverageWaitingTime();
            bestAlgorithm = "Priority";
        }

     // Run Round Robin (use quantum = 4)
        SchedulingResult rrResult = RoundRobin.schedule(processes, 4);
        if (rrResult.getAverageWaitingTime() < bestScore) {
            bestScore = rrResult.getAverageWaitingTime();
            bestAlgorithm = "Round Robin";
        }


        return bestAlgorithm;
    }

    public static String selectWorstAlgorithm(List<Process> processes) {
        double worstScore = Double.MIN_VALUE;
        String worstAlgorithm = "";

        // Run FCFS
        SchedulingResult fcfsResult = FCFS.schedule(processes);
        if (fcfsResult.getAverageWaitingTime() > worstScore) {
            worstScore = fcfsResult.getAverageWaitingTime();
            worstAlgorithm = "FCFS";
        }

        // Run SJF
        SchedulingResult sjfResult = SJF.schedule(processes);
        if (sjfResult.getAverageWaitingTime() > worstScore) {
            worstScore = sjfResult.getAverageWaitingTime();
            worstAlgorithm = "SJF";
        }

        // Run Priority
        SchedulingResult prioResult = PriorityScheduling.schedule(processes);
        if (prioResult.getAverageWaitingTime() > worstScore) {
            worstScore = prioResult.getAverageWaitingTime();
            worstAlgorithm = "Priority";
        }

        // Run Round Robin
        SchedulingResult rrResult = RoundRobin.schedule(processes, 4);
        if (rrResult.getAverageWaitingTime() > worstScore) {
            worstScore = rrResult.getAverageWaitingTime();
            worstAlgorithm = "Round Robin";
        }

        return worstAlgorithm;
    }
}
