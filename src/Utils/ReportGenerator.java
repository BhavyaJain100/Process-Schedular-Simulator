package Utils;

import Models.GanttBlock;
import Models.ProcessResult;
import Models.SchedulingResult;
import Models.Process;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ReportGenerator {

    public static void generateFullReportForAlgorithm(SchedulingResult result, String algorithmName, String filePath) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);
            PDPageContentStream content = new PDPageContentStream(document, page);
            float y = 750, margin = 50;

            content.setFont(PDType1Font.HELVETICA_BOLD, 16);
            content.beginText();
            content.newLineAtOffset(margin, y);
            content.showText("Full Report - Algorithm: " + algorithmName);
            content.endText();
            y -= 30;

            // Process Table from ProcessResult
            content.setFont(PDType1Font.HELVETICA_BOLD, 12);
            y = writeLine(content, "Process Table (PID, Arrival, Burst):", y);
            content.setFont(PDType1Font.HELVETICA, 10);
            y = writeLine(content, "PID   Arrival   Burst", y);
            for (ProcessResult p : result.getProcessResults()) {
                y = writeLine(content, String.format("%-5d %-8d %-7d", p.getPid(), p.getArrivalTime(), p.getBurstTime()), y);
            }

            // Metrics Table
            y -= 20;
            content.setFont(PDType1Font.HELVETICA_BOLD, 12);
            y = writeLine(content, "Metrics Table (Start, End, Waiting, Turnaround):", y);
            content.setFont(PDType1Font.HELVETICA, 10);
            y = writeLine(content, "PID   Start   End   Waiting   Turnaround", y);
            for (ProcessResult p : result.getProcessResults()) {
                y = writeLine(content, String.format("%-5d %-7d %-5d %-9d %-10d",
                        p.getPid(), p.getStartTime(), p.getEndTime(), p.getWaitingTime(), p.getTurnaroundTime()), y);
            }

            // Gantt Chart
            y -= 20;
            content.setFont(PDType1Font.HELVETICA_BOLD, 12);
            y = writeLine(content, "Gantt Chart:", y);
            content.setFont(PDType1Font.HELVETICA, 10);
            StringBuilder gantt = new StringBuilder();
            for (GanttBlock block : result.getGanttChart()) {
                gantt.append("[P").append(block.getLabel())
                        .append(" | ").append(block.getStartTime())
                        .append("-").append(block.getEndTime()).append("] ");
            }
            y = writeLine(content, gantt.toString(), y);

            // Averages
            y -= 20;
            content.setFont(PDType1Font.HELVETICA_BOLD, 12);
            y = writeLine(content, "Averages:", y);
            content.setFont(PDType1Font.HELVETICA, 10);
            y = writeLine(content, "Average Waiting Time: " + result.getAverageWaitingTime(), y);
            y = writeLine(content, "Average Turnaround Time: " + result.getAverageTurnaroundTime(), y);

            content.close();
            document.save(filePath);
            JOptionPane.showMessageDialog(null, "Report saved: " + filePath);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error exporting report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void generateComparisonReport(Map<String, SchedulingResult> results, String filePath) {
        try (PDDocument document = new PDDocument()) {
            PDPage tablePage = new PDPage(PDRectangle.A4);
            document.addPage(tablePage);

            try (PDPageContentStream content = new PDPageContentStream(document, tablePage)) {
                content.setFont(PDType1Font.HELVETICA_BOLD, 16);
                content.beginText();
                content.newLineAtOffset(50, 750);
                content.showText("Scheduling Algorithms Comparison Report");
                content.endText();

                content.setFont(PDType1Font.HELVETICA, 12);
                float y = 720;
                y = writeLine(content, "Algorithm         Avg Waiting Time      Avg Turnaround Time", y);
                for (Map.Entry<String, SchedulingResult> entry : results.entrySet()) {
                    y = writeLine(content, String.format("%-18s %-22.2f %-22.2f",
                            entry.getKey(),
                            entry.getValue().getAverageWaitingTime(),
                            entry.getValue().getAverageTurnaroundTime()), y);
                }
            }

            BufferedImage chartImage = createComparisonChart(results);
            File chartFile = File.createTempFile("chart", ".png");
            ImageIO.write(chartImage, "png", chartFile);

            PDPage chartPage = new PDPage(PDRectangle.A4);
            document.addPage(chartPage);
            PDFUtils.addImageToPage(document, chartPage, chartFile.getAbsolutePath());
            chartFile.deleteOnExit();

            document.save(filePath);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to generate comparison report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void generateCompleteReport(Map<String, SchedulingResult> results, Map<String, List<Process>> processMap, String filePath) {
        try (PDDocument document = new PDDocument()) {
            float margin = 50;

            for (Map.Entry<String, SchedulingResult> entry : results.entrySet()) {
                String algo = entry.getKey();
                SchedulingResult result = entry.getValue();
                List<Process> processes = processMap.get(algo);

                PDPage page = new PDPage(PDRectangle.LETTER);
                document.addPage(page);
                PDPageContentStream content = new PDPageContentStream(document, page);
                float y = 750;

                content.setFont(PDType1Font.HELVETICA_BOLD, 16);
                content.beginText();
                content.newLineAtOffset(margin, y);
                content.showText("Report - Algorithm: " + algo);
                content.endText();
                y -= 30;

                // Process Table
                content.setFont(PDType1Font.HELVETICA_BOLD, 12);
                y = writeLine(content, "Process Table (PID, Arrival, Burst, Priority):", y);
                content.setFont(PDType1Font.HELVETICA, 10);
                y = writeLine(content, "PID   Arrival   Burst   Priority", y);
                for (Process p : processes) {
                    y = writeLine(content, String.format("%-5d %-8d %-7d %-8d",
                            p.getId(), p.getArrivalTime(), p.getBurstTime(), p.getPriority()), y);
                }

                // Metrics Table
                y -= 20;
                content.setFont(PDType1Font.HELVETICA_BOLD, 12);
                y = writeLine(content, "Metrics Table (Start, End, Waiting, Turnaround):", y);
                content.setFont(PDType1Font.HELVETICA, 10);
                y = writeLine(content, "PID   Start   End   Waiting   Turnaround", y);
                for (ProcessResult p : result.getProcessResults()) {
                    y = writeLine(content, String.format("%-5d %-7d %-5d %-9d %-10d",
                            p.getPid(), p.getStartTime(), p.getEndTime(),
                            p.getWaitingTime(), p.getTurnaroundTime()), y);
                }

                // Gantt Chart
                y -= 20;
                content.setFont(PDType1Font.HELVETICA_BOLD, 12);
                y = writeLine(content, "Gantt Chart:", y);
                content.setFont(PDType1Font.HELVETICA, 10);
                StringBuilder gantt = new StringBuilder();
                for (GanttBlock block : result.getGanttChart()) {
                    gantt.append("[P").append(block.getLabel())
                            .append(" | ").append(block.getStartTime())
                            .append("-").append(block.getEndTime()).append("] ");
                }
                y = writeLine(content, gantt.toString(), y);

                // Averages
                y -= 20;
                content.setFont(PDType1Font.HELVETICA_BOLD, 12);
                y = writeLine(content, "Averages:", y);
                content.setFont(PDType1Font.HELVETICA, 10);
                y = writeLine(content, "Average Waiting Time: " + result.getAverageWaitingTime(), y);
                y = writeLine(content, "Average Turnaround Time: " + result.getAverageTurnaroundTime(), y);

                content.close();
            }

            // Comparison Table Page
            PDPage tablePage = new PDPage(PDRectangle.LETTER);
            document.addPage(tablePage);
            PDPageContentStream tableContent = new PDPageContentStream(document, tablePage);
            float y = 750;

            tableContent.setFont(PDType1Font.HELVETICA_BOLD, 16);
            tableContent.beginText();
            tableContent.newLineAtOffset(50, y);
            tableContent.showText("Comparison Table");
            tableContent.endText();
            y -= 30;

            tableContent.setFont(PDType1Font.HELVETICA, 12);
            y = writeLine(tableContent, "Algorithm         Avg Waiting Time      Avg Turnaround Time", y);
            for (Map.Entry<String, SchedulingResult> entry : results.entrySet()) {
                y = writeLine(tableContent, String.format("%-18s %-22.2f %-22.2f",
                        entry.getKey(),
                        entry.getValue().getAverageWaitingTime(),
                        entry.getValue().getAverageTurnaroundTime()), y);
            }
            tableContent.close();

            // Comparison Chart Page
            BufferedImage chartImage = createComparisonChart(results);
            File chartFile = File.createTempFile("chart", ".png");
            ImageIO.write(chartImage, "png", chartFile);

            PDPage chartPage = new PDPage(PDRectangle.LETTER);
            document.addPage(chartPage);
            PDFUtils.addImageToPage(document, chartPage, chartFile.getAbsolutePath());
            chartFile.deleteOnExit();

            document.save(filePath);
            JOptionPane.showMessageDialog(null, "Full Report saved: " + filePath);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to generate full report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static float writeLine(PDPageContentStream content, String text, float y) throws IOException {
        if (y < 60) return y;
        content.beginText();
        content.newLineAtOffset(50, y);
        content.showText(text);
        content.endText();
        return y - 15;
    }

    private static BufferedImage createComparisonChart(Map<String, SchedulingResult> results) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, SchedulingResult> entry : results.entrySet()) {
            String algo = entry.getKey();
            dataset.addValue(entry.getValue().getAverageWaitingTime(), "Average Waiting Time", algo);
            dataset.addValue(entry.getValue().getAverageTurnaroundTime(), "Average Turnaround Time", algo);
        }
        JFreeChart chart = ChartFactory.createBarChart(
                "Algorithm Comparison",
                "Algorithm",
                "Time (ms)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );
        return chart.createBufferedImage(500, 300);
    }
}
