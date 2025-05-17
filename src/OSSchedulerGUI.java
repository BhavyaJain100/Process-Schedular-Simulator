import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;


class Process {
    int id, arrivalTime, burstTime, priority, completionTime, waitingTime, turnAroundTime, remainingTime;

    public Process(int id, int arrivalTime, int burstTime, int priority) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.priority = priority;
    }
}

public class OSSchedulerGUI extends JFrame {
    private JTextArea outputArea;
    private JComboBox<String> algoBox;
    private JTextField arrivalField, burstField, priorityField, quantumField;
    private DefaultListModel<Process> processListModel = new DefaultListModel<>();

    public OSSchedulerGUI() {
        setTitle("OS Scheduler Project");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top input panel
        JPanel topPanel = new JPanel(new GridLayout(2, 5));
        topPanel.add(new JLabel("Arrival Time:"));
        arrivalField = new JTextField();
        topPanel.add(arrivalField);

        topPanel.add(new JLabel("Burst Time:"));
        burstField = new JTextField();
        topPanel.add(burstField);

        topPanel.add(new JLabel("Priority:"));
        priorityField = new JTextField();
        topPanel.add(priorityField);

        topPanel.add(new JLabel("Quantum (for RR):"));
        quantumField = new JTextField();
        topPanel.add(quantumField);

        JButton addButton = new JButton("Add Process");
        topPanel.add(addButton);

        JButton clearButton = new JButton("Clear All");
        topPanel.add(clearButton);

        add(topPanel, BorderLayout.NORTH);

        // Center list and output
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom panel
        JPanel bottomPanel = new JPanel();
        algoBox = new JComboBox<>(new String[]{"FCFS", "SJF", "Round Robin", "Priority"});
        bottomPanel.add(algoBox);

        JButton runButton = new JButton("Run Scheduler");
        bottomPanel.add(runButton);

        add(bottomPanel, BorderLayout.SOUTH);

        // Action Listeners
        addButton.addActionListener(e -> addProcess());
        runButton.addActionListener(e -> runScheduler());
        clearButton.addActionListener(e -> {
            processListModel.clear();
            outputArea.setText("");
        });

        setVisible(true);
    }

    private void addProcess() {
        try {
            int arrival = Integer.parseInt(arrivalField.getText());
            int burst = Integer.parseInt(burstField.getText());
            int priority = Integer.parseInt(priorityField.getText());
            Process p = new Process(processListModel.size() + 1, arrival, burst, priority);
            processListModel.addElement(p);
            outputArea.append("Added: P" + p.id + "\n");
            arrivalField.setText("");
            burstField.setText("");
            priorityField.setText("");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Enter valid numbers.");
        }
    }

    private void runScheduler() {
        List<Process> processes = Collections.list(processListModel.elements());
        String algo = (String) algoBox.getSelectedItem();

        switch (algo) {
            case "FCFS":
                runFCFS(processes);
                break;
            case "SJF":
                runSJF(processes);
                break;
            case "Round Robin":
                try {
                    int quantum = Integer.parseInt(quantumField.getText());
                    runRoundRobin(processes, quantum);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Enter valid quantum.");
                }
                break;
            case "Priority":
                runPriority(processes);
                break;
        }
    }

    private void runFCFS(List<Process> processes) {
        outputArea.setText("Running FCFS...\n");
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));

        int currentTime = 0;
        for (Process p : processes) {
            currentTime = Math.max(currentTime, p.arrivalTime);
            p.completionTime = currentTime + p.burstTime;
            p.turnAroundTime = p.completionTime - p.arrivalTime;
            p.waitingTime = p.turnAroundTime - p.burstTime;
            currentTime += p.burstTime;
            outputArea.append("P" + p.id + ": CT=" + p.completionTime + " WT=" + p.waitingTime + " TAT=" + p.turnAroundTime + "\n");
        }
    }

    private void runSJF(List<Process> processes) {
        outputArea.setText("Running SJF...\n");
        List<Process> queue = new ArrayList<>();
        int currentTime = 0;
        int completed = 0;
        boolean[] visited = new boolean[processes.size()];

        while (completed < processes.size()) {
            for (int i = 0; i < processes.size(); i++) {
                Process p = processes.get(i);
                if (!visited[i] && p.arrivalTime <= currentTime) {
                    queue.add(p);
                    visited[i] = true;
                }
            }

            if (queue.isEmpty()) {
                currentTime++;
                continue;
            }

            queue.sort(Comparator.comparingInt(p -> p.burstTime));
            Process p = queue.remove(0);
            currentTime = Math.max(currentTime, p.arrivalTime);
            p.completionTime = currentTime + p.burstTime;
            p.turnAroundTime = p.completionTime - p.arrivalTime;
            p.waitingTime = p.turnAroundTime - p.burstTime;
            currentTime += p.burstTime;
            completed++;

            outputArea.append("P" + p.id + ": CT=" + p.completionTime + " WT=" + p.waitingTime + " TAT=" + p.turnAroundTime + "\n");
        }
    }

    private void runRoundRobin(List<Process> processes, int quantum) {
        outputArea.setText("Running Round Robin...\n");
        Queue<Process> queue = new LinkedList<>();
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));
        int currentTime = 0, completed = 0;
        boolean[] visited = new boolean[processes.size()];

        while (completed < processes.size()) {
            for (int i = 0; i < processes.size(); i++) {
                if (!visited[i] && processes.get(i).arrivalTime <= currentTime) {
                    queue.add(processes.get(i));
                    visited[i] = true;
                }
            }

            if (queue.isEmpty()) {
                currentTime++;
                continue;
            }

            Process p = queue.poll();
            int execTime = Math.min(p.remainingTime, quantum);
            currentTime += execTime;
            p.remainingTime -= execTime;

            for (int i = 0; i < processes.size(); i++) {
                if (!visited[i] && processes.get(i).arrivalTime <= currentTime) {
                    queue.add(processes.get(i));
                    visited[i] = true;
                }
            }

            if (p.remainingTime > 0) {
                queue.add(p);
            } else {
                p.completionTime = currentTime;
                p.turnAroundTime = p.completionTime - p.arrivalTime;
                p.waitingTime = p.turnAroundTime - p.burstTime;
                outputArea.append("P" + p.id + ": CT=" + p.completionTime + " WT=" + p.waitingTime + " TAT=" + p.turnAroundTime + "\n");
                completed++;
            }
        }
    }

    private void runPriority(List<Process> processes) {
        outputArea.setText("Running Priority Scheduling...\n");
        List<Process> queue = new ArrayList<>();
        int currentTime = 0;
        int completed = 0;
        boolean[] visited = new boolean[processes.size()];

        while (completed < processes.size()) {
            for (int i = 0; i < processes.size(); i++) {
                if (!visited[i] && processes.get(i).arrivalTime <= currentTime) {
                    queue.add(processes.get(i));
                    visited[i] = true;
                }
            }

            if (queue.isEmpty()) {
                currentTime++;
                continue;
            }

            queue.sort(Comparator.comparingInt(p -> p.priority));
            Process p = queue.remove(0);
            currentTime = Math.max(currentTime, p.arrivalTime);
            p.completionTime = currentTime + p.burstTime;
            p.turnAroundTime = p.completionTime - p.arrivalTime;
            p.waitingTime = p.turnAroundTime - p.burstTime;
            currentTime += p.burstTime;
            completed++;

            outputArea.append("P" + p.id + ": CT=" + p.completionTime + " WT=" + p.waitingTime + " TAT=" + p.turnAroundTime + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(OSSchedulerGUI::new);
    }
}
