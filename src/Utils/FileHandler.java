// File: Utils/FileHandler.java
package Utils;

import Models.ProcessResult;
import Models.GanttBlock;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.util.List;


import Models.Process;
import Models.SchedulingResult;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.util.ArrayList;

public class FileHandler {

	public static File chooseProcessFile() {
	    JFileChooser fileChooser = new JFileChooser();
	    fileChooser.setDialogTitle("Import Process Table");
	    fileChooser.setFileFilter(new FileNameExtensionFilter(
	            "Supported Files (Word, PDF, CSV, TXT)", "docx", "pdf", "csv", "txt"));
	    if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
	        return fileChooser.getSelectedFile();
	    }
	    return null;
	}

	public static List<Process> importProcesses(File file) {
	    List<Process> processes = new ArrayList<>();
	    try {
	        String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
	        switch (extension.toLowerCase()) {
	            case "docx":
	                return importFromWord(file);
	            case "pdf":
	                return importFromPDF(file);
	            case "csv":
	            case "txt":
	                return importFromText(file);
	            default:
	                JOptionPane.showMessageDialog(null, "Unsupported file format");
	        }
	    } catch (Exception e) {
	        JOptionPane.showMessageDialog(null, "Error importing file: " + e.getMessage());
	    }
	    return processes;
	}


    private static List<Process> importFromWord(File file) throws Exception {
        List<Process> processes = new ArrayList<>();
        int[] id = {1}; // Use array to mutate inside lambda

        // Read the Word document
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {

            // 1. Process paragraphs (e.g., "1 2 3")
            for (XWPFParagraph para : document.getParagraphs()) {
                String line = para.getText().trim();
                if (line.matches("^\\d+\\s+\\d+\\s+\\d+$")) {
                    String[] parts = line.split("\\s+");
                    int arrival = Integer.parseInt(parts[0]);
                    int burst = Integer.parseInt(parts[1]);
                    int priority = Integer.parseInt(parts[2]);
                    processes.add(new Process(id[0]++, arrival, burst, priority));
                }
            }

            // 2. Process tables (structured format)
            document.getTables().forEach(table -> {
                for (int i = 1; i < table.getNumberOfRows(); i++) { // Skip header row
                    try {
                        String arrivalStr = table.getRow(i).getCell(1).getText().trim();
                        String burstStr = table.getRow(i).getCell(2).getText().trim();
                        String priorityStr = table.getRow(i).getCell(3).getText().trim();

                        int arrival = Integer.parseInt(arrivalStr);
                        int burst = Integer.parseInt(burstStr);
                        int priority = Integer.parseInt(priorityStr);

                        processes.add(new Process(id[0]++, arrival, burst, priority));
                    } catch (Exception e) {
                        System.err.println("Skipping invalid row " + i + ": " + e.getMessage());
                    }
                }
            });
        }

        return processes;
    }



    private static List<Process> importFromPDF(File file) throws Exception {
        List<Process> processes = new ArrayList<>();
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            int id = 1;
            String[] lines = text.split("\\r?\\n");
            for (String line : lines) {
                line = line.trim();
                if (line.matches("^\\d+\\s+\\d+\\s+\\d+$")) {
                    String[] parts = line.split("\\s+");
                    int arrival = Integer.parseInt(parts[0]);
                    int burst = Integer.parseInt(parts[1]);
                    int priority = Integer.parseInt(parts[2]);
                    processes.add(new Process(id++, arrival, burst, priority));
                }
            }
        }
        return processes;
    }

    private static List<Process> importFromText(File file) throws Exception {
        List<Process> processes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int id = 1;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.matches("^\\d+\\s+\\d+\\s+\\d+$")) {
                    String[] parts = line.split("\\s+");
                    int arrival = Integer.parseInt(parts[0]);
                    int burst = Integer.parseInt(parts[1]);
                    int priority = Integer.parseInt(parts[2]);
                    processes.add(new Process(id++, arrival, burst, priority));
                }
            }
        }
        return processes;
    }

    public static void exportToPDF(SchedulingResult result, String algorithm) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save PDF Report");
        fileChooser.setSelectedFile(new File(algorithm + "_Report.pdf"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));

        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage(PDRectangle.LETTER);
                document.addPage(page);

                PDPageContentStream content = new PDPageContentStream(document, page);
                content.setFont(PDType1Font.HELVETICA_BOLD, 14);
                content.beginText();
                content.newLineAtOffset(50, 750);
                content.showText("Scheduling Report - Algorithm: " + algorithm);
                content.endText();

                int y = 720;
                content.setFont(PDType1Font.HELVETICA, 10);

                // Table Header
                String header = String.format("%-5s %-7s %-7s %-9s %-7s %-6s %-9s %-11s",
                        "ID", "Arrival", "Burst", "Priority", "Start", "End", "Waiting", "Turnaround");
                y = drawText(content, header, y, true);

                // Table Rows
                for (ProcessResult p : result.getProcessResults()) {
                    String row = String.format("%-5d %-7d %-7d %-9d %-7d %-6d %-9d %-11d",
                            p.getPid(), p.getArrivalTime(), p.getBurstTime(),
                            p.getStartTime(), p.getEndTime(), p.getWaitingTime(), p.getTurnaroundTime());
                    y = drawText(content, row, y, false);
                    if (y < 100) break; // Avoid overflow (basic)
                }

                // Gantt Chart
                y -= 20;
                drawText(content, "Gantt Chart:", y, true);
                y -= 15;

                StringBuilder gantt = new StringBuilder();
                for (GanttBlock block : result.getGanttChart()) {
                    gantt.append("[P").append(block.getLabel())
                         .append(" | ").append(block.getStartTime())
                         .append("-").append(block.getEndTime()).append("] ");
                }
                y = drawText(content, gantt.toString(), y, false);

                // Metrics
                y -= 30;
                drawText(content, "Average Waiting Time: " + result.getAverageWaitingTime(), y, false);
                y -= 15;
                drawText(content, "Average Turnaround Time: " + result.getAverageTurnaroundTime(), y, false);

                content.close();
                document.save(file);
                JOptionPane.showMessageDialog(null, "PDF saved: " + file.getAbsolutePath());

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error writing PDF: " + e.getMessage());
            }
        }
    }

    private static int drawText(PDPageContentStream content, String text, int y, boolean bold) throws IOException {
        if (y < 60) return y;
        content.beginText();
        content.setFont(bold ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA, 10);
        content.newLineAtOffset(50, y);
        content.showText(text);
        content.endText();
        return y - 15;
    }

}
