package Utils;

import Models.Process;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ProcessGenerator {

    public static List<Process> generate(int count) {
        List<Process> processes = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < count; i++) {
            int id = i + 1;
            int arrivalTime = rand.nextInt(10);       // Arrival between 0-9
            int burstTime = rand.nextInt(20) + 1;     // Burst between 1-20
            int priority = rand.nextInt(5) + 1;       // Priority between 1-5

            processes.add(new Process(id, arrivalTime, burstTime, priority));
        }

        return processes;
    }
}
