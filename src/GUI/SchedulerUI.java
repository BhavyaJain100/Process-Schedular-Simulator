package GUI;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.*;
import java.util.List;
import Models.*;
import Algorithms.*;
import Utils.*;

public class SchedulerUI extends JFrame {
    private JPanel inputPanel, controlPanel, visualizationPanel;
    private JButton btnAddProcess, btnGenerate, btnRun, btnClear;
    private JComboBox<String> algorithmSelector;
    private JRadioButton manualRadio, autoRadio, bestRadio, worstRadio;
    private ButtonGroup modeGroup, autoChoiceGroup;
    private List<Models.Process> processes = new ArrayList<>();
    private int processCounter = 1;
    
    // Visualization components
    private JTable processTable, metricsTable, comparisonTable;
    private DefaultTableModel processTableModel, metricsTableModel, comparisonTableModel;
    private GanttChartPanel ganttPanel;
    private JLabel metricsLabel;
    private JTextField quantumField;
    private JTabbedPane resultTabs;

    public SchedulerUI() {
        setTitle("Smart Process Scheduler");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        initUI();
        setVisible(true);
    }

    private void initUI() {
        setLayout(new BorderLayout(5, 5));

        // Input Panel
        inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        btnAddProcess = createStyledButton("Add Manual Process");
        btnGenerate = createStyledButton("Auto Generate");
        btnClear = createStyledButton("Clear All");
        JButton btnExportPDF = createStyledButton("Generate PDF Report");
        btnExportPDF.addActionListener(e -> generatePDFReport());

        btnAddProcess.addActionListener(e -> addManualProcess());
        btnGenerate.addActionListener(e -> autoGenerateProcesses());
        btnClear.addActionListener(e -> clearAllProcesses());

        inputPanel.add(btnAddProcess);
        inputPanel.add(btnGenerate);
        inputPanel.add(btnClear);
        inputPanel.add(btnExportPDF);

        // Process Table
        String[] processColumns = {"PID", "Arrival Time", "Burst Time", "Priority"};
        processTableModel = new DefaultTableModel(processColumns, 0);
        processTable = new JTable(processTableModel);
        processTable.setAutoCreateRowSorter(true);
        JScrollPane tableScroll = new JScrollPane(processTable);
        tableScroll.setPreferredSize(new Dimension(1150, 150));

        // Control Panel
        controlPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        JPanel algorithmPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JPanel configPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        // Algorithm Selection
        algorithmSelector = new JComboBox<>(new String[]{"Select Algorithm","FCFS", "SJF", "Priority", "Round Robin"});
        algorithmSelector.setEnabled(true);
        algorithmSelector.addActionListener(e -> toggleQuantumField());
        
        manualRadio = new JRadioButton("Manual Select");
        autoRadio = new JRadioButton("Auto Select");
        bestRadio = new JRadioButton("Best Fit");
        worstRadio = new JRadioButton("Worst Fit");
        quantumField = new JTextField("4", 5);
        quantumField.setEnabled(false);
        
        algorithmSelector.addItemListener(e -> {
            if (manualRadio.isSelected()) {
                String selectedAlgo = (String) algorithmSelector.getSelectedItem();
                quantumField.setEnabled("Round Robin".equals(selectedAlgo));
            }
        });
        
        
     // Manual/Auto selection mode listener
        ItemListener selectionModeListener = e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                boolean isManual = manualRadio.isSelected();
                algorithmSelector.setEnabled(isManual);

                String selectedAlgo = (String) algorithmSelector.getSelectedItem();
                quantumField.setEnabled(isManual && "Round Robin".equals(selectedAlgo));
            }
        };
        

        manualRadio.addItemListener(selectionModeListener);
        autoRadio.addItemListener(selectionModeListener);
        
        
        modeGroup = new ButtonGroup();
        modeGroup.add(manualRadio);
        modeGroup.add(autoRadio);
        manualRadio.setSelected(true);
        
        autoChoiceGroup = new ButtonGroup();
        autoChoiceGroup.add(bestRadio);
        autoChoiceGroup.add(worstRadio);
        
        btnRun = createStyledButton("Run Scheduling");
        btnRun.addActionListener(e -> runScheduler());

        algorithmPanel.add(new JLabel("Scheduling Mode:"));
        algorithmPanel.add(manualRadio);
        algorithmPanel.add(autoRadio);
        algorithmPanel.add(new JLabel("Algorithm:"));
        algorithmPanel.add(algorithmSelector);
        algorithmPanel.add(new JLabel("Round Robin Quantum:"));
        algorithmPanel.add(quantumField);
        algorithmPanel.add(btnRun);
        
        configPanel.add(new JLabel("Auto Selection Criteria:"));
        configPanel.add(bestRadio);
        configPanel.add(worstRadio);
        
              
        JButton importButton = new JButton("Import Process Table");
        importButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Import Process Table");
            fileChooser.setFileFilter(new FileNameExtensionFilter(
                    "Supported Files (Word, PDF, CSV, TXT)", "docx", "pdf", "csv", "txt"));

            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    List<Models.Process> importedProcesses = FileHandler.importProcesses(selectedFile);

                    if (importedProcesses.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "No valid processes found in file!", "Info", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    // ✅ Update internal process list
                    processes.clear();
                    processes.addAll(importedProcesses);

                    // ✅ Update UI Table
                    DefaultTableModel model = (DefaultTableModel) processTable.getModel();
                    model.setRowCount(0); // Clear table rows

                    for (Models.Process p : importedProcesses) {
                        model.addRow(new Object[]{
                            p.getId(),
                            p.getArrivalTime(),
                            p.getBurstTime(),
                            p.getPriority()
                        });
                    }

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null,
                            "Failed to import: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });


        
        algorithmPanel.add(importButton); // Add to control panel or menu
        
        controlPanel.add(algorithmPanel);
        controlPanel.add(configPanel);


        // Visualization Panel
        visualizationPanel = new JPanel(new BorderLayout());
        ganttPanel = new GanttChartPanel();
        metricsLabel = new JLabel(" ", JLabel.CENTER);
        metricsLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        
        resultTabs = new JTabbedPane();
        resultTabs.addTab("Gantt Chart", new JScrollPane(ganttPanel));
        resultTabs.addTab("Metrics", createMetricsPanel());
        resultTabs.addTab("Process Table", tableScroll);
        resultTabs.addTab("Algorithm Comparison", createComparisonPanel());

        visualizationPanel.add(resultTabs, BorderLayout.CENTER);
        visualizationPanel.add(metricsLabel, BorderLayout.SOUTH);

        // Initialize quantum field state
        toggleQuantumField();
        
        // Main Layout
        add(inputPanel, BorderLayout.NORTH);
        add(controlPanel, BorderLayout.CENTER);
        add(visualizationPanel, BorderLayout.SOUTH);
    }

    private void toggleQuantumField() {
        boolean isRR = "Round Robin".equals(algorithmSelector.getSelectedItem());
        quantumField.setEnabled(isRR);
    }

    private JPanel createComparisonPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columns = {"Algorithm", "Avg Waiting Time", "Avg Turnaround Time"};
        comparisonTableModel = new DefaultTableModel(columns, 0);
        comparisonTable = new JTable(comparisonTableModel);
        JScrollPane scroll = new JScrollPane(comparisonTable);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(70, 130, 180));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }

    private JPanel createMetricsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columns = {"PID", "Arrival", "Burst", "Start", "End", "Waiting", "Turnaround"};
        metricsTableModel = new DefaultTableModel(columns, 0);
        metricsTable = new JTable(metricsTableModel);
        JScrollPane scroll = new JScrollPane(metricsTable);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void addManualProcess() {
        try {
            String input = JOptionPane.showInputDialog(this, "How many processes do you want to add manually?");
            if (input == null) return; // User canceled
            int count = Integer.parseInt(input);

            if (count <= 0) throw new NumberFormatException();

            for (int i = 0; i < count; i++) {
                JPanel inputDialog = new JPanel(new GridLayout(3, 2));
                JTextField arrivalField = new JTextField();
                JTextField burstField = new JTextField();
                JTextField priorityField = new JTextField();

                inputDialog.add(new JLabel("Arrival Time:"));
                inputDialog.add(arrivalField);
                inputDialog.add(new JLabel("Burst Time:"));
                inputDialog.add(burstField);
                inputDialog.add(new JLabel("Priority:"));
                inputDialog.add(priorityField);

                int result = JOptionPane.showConfirmDialog(this, inputDialog,
                        "Add Process " + (i + 1) + " of " + count, JOptionPane.OK_CANCEL_OPTION);

                if (result == JOptionPane.OK_OPTION) {
                    int arrival = Integer.parseInt(arrivalField.getText());
                    int burst = Integer.parseInt(burstField.getText());
                    int priority = Integer.parseInt(priorityField.getText());

                    if (arrival < 0 || burst <= 0 || priority <= 0) {
                        JOptionPane.showMessageDialog(this,
                                "Invalid input! Please enter positive integers.");
                        i--; // Retry this iteration
                        continue;
                    }

                    Models.Process p = new Models.Process(processCounter++, arrival, burst, priority);
                    processes.add(p);
                    updateProcessTable(p);
                } else {
                    break; // User cancelled input mid-way
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid input! Please enter a positive integer for the number of processes.");
        }
    }

    private void autoGenerateProcesses() {
        try {
            int count = Integer.parseInt(JOptionPane.showInputDialog(this, 
                "Number of processes to generate:"));
            
            List<Models.Process> generated = ProcessGenerator.generate(count);
            for(Models.Process p : generated) {
                p.setId(processCounter++);
                processes.add(p);
                updateProcessTable(p);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid number!");
        }
    }

    private void updateProcessTable(Models.Process p) {
        processTableModel.addRow(new Object[]{
            p.getId(),
            p.getArrivalTime(),
            p.getBurstTime(),
            p.getPriority()
        });
    }

    private void clearAllProcesses() {
        processes.clear();
        processCounter = 1;
        processTableModel.setRowCount(0);
        metricsTableModel.setRowCount(0);
        comparisonTableModel.setRowCount(0);
        ganttPanel.clearChart();
        metricsLabel.setText(" ");
    }

    private void runScheduler() {
        if(processes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No processes to schedule!");
            return;
        }

        try {
            int quantum = Integer.parseInt(quantumField.getText());
            if (quantum <= 0) {
                throw new NumberFormatException("Quantum must be positive");
            }

            SchedulingResult result;
            String selectedAlgo = "";

            if(manualRadio.isSelected()) {
                selectedAlgo = (String) algorithmSelector.getSelectedItem();
            } else {
                boolean best = bestRadio.isSelected();
                selectedAlgo = best ? AlgorithmSelector.selectBestAlgorithm(processes) 
                                  : AlgorithmSelector.selectWorstAlgorithm(processes);
                JOptionPane.showMessageDialog(this, 
                    "Selected Algorithm: " + selectedAlgo + " (" + (best ? "Best" : "Worst") + ")");
            }

            switch(selectedAlgo) {
                case "FCFS": 
                    result = FCFS.schedule(processes);
                    break;
                case "SJF": 
                    result = SJF.schedule(processes);
                    break;
                case "Priority": 
                    result = PriorityScheduling.schedule(processes);
                    break;
                case "Round Robin": 
                    result = RoundRobin.schedule(processes, quantum);
                    break;
                default: 
                    throw new IllegalArgumentException("Invalid algorithm");
            }

            updateVisualization(result, selectedAlgo);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid quantum value. Please enter a positive integer.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error in scheduling: " + ex.getMessage());
        }
    }

    private void updateVisualization(SchedulingResult result, String algorithm) {
        // Update Gantt Chart
        ganttPanel.setGanttBlocks(result.getGanttChart());
        
        // Update Metrics
        metricsTableModel.setRowCount(0);
        for(ProcessResult pr : result.getProcessResults()) {
            metricsTableModel.addRow(new Object[]{
                pr.getPid(),
                pr.getArrivalTime(),
                pr.getBurstTime(),
                pr.getStartTime(),
                pr.getEndTime(),
                pr.getWaitingTime(),
                pr.getTurnaroundTime()
            });
        }
        
        String metrics = String.format("<html><b>Algorithm:</b> %s<br>"
            + "<b>Average Waiting Time:</b> %.2f<br>"
            + "<b>Average Turnaround Time:</b> %.2f</html>",
            algorithm,
            result.getAverageWaitingTime(),
            result.getAverageTurnaroundTime());
        
        metricsLabel.setText(metrics);
        resultTabs.setSelectedIndex(0); // Switch to Gantt Chart tab
    }
    
    private List<Models.Process> deepCopyProcesses(List<Models.Process> original) {
        List<Models.Process> copy = new ArrayList<>();
        for (Models.Process p : original) {
            copy.add(new Models.Process(p.getId(), p.getArrivalTime(), p.getBurstTime(), p.getPriority()));
        }
        return copy;
    }


    private void generatePDFReport() {
        if (processes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No processes available to generate report.");
            return;
        }

        try {
            int quantum = Integer.parseInt(quantumField.getText());
            if (quantum <= 0) {
                throw new NumberFormatException("Quantum must be positive");
            }

            // Create deep copies of the original process list for each algorithm
            Map<String, SchedulingResult> allResults = new LinkedHashMap<>();
            Map<String, List<Models.Process>> allProcesses = new LinkedHashMap<>();

            List<Models.Process> fcfsProcesses = deepCopyProcesses(processes);
            allResults.put("FCFS", FCFS.schedule(fcfsProcesses));
            allProcesses.put("FCFS", fcfsProcesses);

            List<Models.Process> sjfProcesses = deepCopyProcesses(processes);
            allResults.put("SJF", SJF.schedule(sjfProcesses));
            allProcesses.put("SJF", sjfProcesses);

            List<Models.Process> priorityProcesses = deepCopyProcesses(processes);
            allResults.put("Priority", PriorityScheduling.schedule(priorityProcesses));
            allProcesses.put("Priority", priorityProcesses);

            List<Models.Process> rrProcesses = deepCopyProcesses(processes);
            allResults.put("Round Robin", RoundRobin.schedule(rrProcesses, quantum));
            allProcesses.put("Round Robin", rrProcesses);

            // Update the comparison table in the UI
            comparisonTableModel.setRowCount(0);
            for (Map.Entry<String, SchedulingResult> entry : allResults.entrySet()) {
                comparisonTableModel.addRow(new Object[]{
                    entry.getKey(),
                    String.format("%.2f", entry.getValue().getAverageWaitingTime()),
                    String.format("%.2f", entry.getValue().getAverageTurnaroundTime())
                });
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save PDF Report");
            fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));

            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String path = selectedFile.getAbsolutePath();
                if (!path.endsWith(".pdf")) {
                    path += ".pdf";
                }

                ReportGenerator.generateCompleteReport(allResults, allProcesses, path);

                int openOption = JOptionPane.showConfirmDialog(this,
                    "Report generated successfully.\nOpen it now?", "Success", JOptionPane.YES_NO_OPTION);
                if (openOption == JOptionPane.YES_OPTION) {
                    Desktop.getDesktop().open(new File(path));
                }
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid quantum value. Please enter a positive integer.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error generating report: " + ex.getMessage());
            ex.printStackTrace();
        }
    }


    // Custom Gantt Chart Panel with dynamic scaling
    class GanttChartPanel extends JPanel {
        private List<GanttBlock> ganttBlocks = new ArrayList<>();

        public void setGanttBlocks(List<GanttBlock> blocks) {
            this.ganttBlocks = blocks;
            revalidate();
            repaint();
        }

        public void clearChart() {
            ganttBlocks.clear();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if(ganttBlocks.isEmpty()) return;

            int y = 50;
            int height = 40;
            int panelWidth = getWidth();
            int maxTime = ganttBlocks.get(ganttBlocks.size()-1).getEndTime();
            int timeScale = Math.max(20, (panelWidth - 100) / Math.max(1, maxTime));

            // Draw blocks
            for(GanttBlock block : ganttBlocks) {
                int xStart = block.getStartTime() * timeScale + 50;
                int width = (block.getEndTime() - block.getStartTime()) * timeScale;
                
                g.setColor(getRandomColor(block.getLabel().hashCode()));
                g.fillRect(xStart, y, width, height);
                g.setColor(Color.BLACK);
                g.drawRect(xStart, y, width, height);
                
                // Draw process ID
                String label = block.getLabel();
                FontMetrics fm = g.getFontMetrics();
                int labelWidth = fm.stringWidth(label);
                g.drawString(label, 
                    xStart + (width - labelWidth)/2, 
                    y + height/2 + 5);
            }

            // Draw timeline
            int timelineY = y + height + 20;
            g.drawLine(50, timelineY, 50 + maxTime * timeScale, timelineY);
            
            // Draw time markers
            for(int t=0; t<=maxTime; t++) {
                int x = 50 + t * timeScale;
                g.drawLine(x, timelineY - 5, x, timelineY + 5);
                if(t % 5 == 0 || t == maxTime) {
                    String timeLabel = Integer.toString(t);
                    int labelWidth = g.getFontMetrics().stringWidth(timeLabel);
                    g.drawString(timeLabel, x - labelWidth/2, timelineY + 20);
                }
            }
        }

        private Color getRandomColor(int seed) {
            Random rand = new Random(seed);
            return new Color(rand.nextInt(200), rand.nextInt(200), rand.nextInt(200));
        }

        @Override
        public Dimension getPreferredSize() {
            if(ganttBlocks.isEmpty()) return new Dimension(800, 200);
            int maxTime = ganttBlocks.get(ganttBlocks.size()-1).getEndTime();
            return new Dimension(maxTime * 40 + 100, 200);
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new SchedulerUI();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
