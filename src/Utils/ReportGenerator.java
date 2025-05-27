package Utils;

import Models.SchedulingResult;
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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ReportGenerator {

    public static void generateComparisonReport(Map<String, SchedulingResult> results, String filePath) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("Scheduling Algorithms Comparison Report");
                contentStream.endText();

                addComparisonTable(contentStream, results);
            }

            // Create chart and save it to temporary PNG
            BufferedImage chartImage = createComparisonChart(results);
            File chartFile = File.createTempFile("chart", ".png");
            ImageIO.write(chartImage, "png", chartFile);

            PDPage chartPage = new PDPage(PDRectangle.A4);
            document.addPage(chartPage);

            PDFUtils.addImageToPage(document, chartPage, chartFile.getAbsolutePath());

            chartFile.deleteOnExit();
            document.save(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addComparisonTable(PDPageContentStream contentStream,
                                           Map<String, SchedulingResult> results) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        float y = 720;
        float margin = 50;

        contentStream.beginText();
        contentStream.newLineAtOffset(margin, y);
        contentStream.showText("Algorithm         Avg Waiting Time      Avg Turnaround Time");
        contentStream.endText();

        for (Map.Entry<String, SchedulingResult> entry : results.entrySet()) {
            y -= 20;
            String algorithm = entry.getKey();
            SchedulingResult result = entry.getValue();

            String row = String.format("%-18s %-22.2f %-22.2f",
                    algorithm,
                    result.getAverageWaitingTime(),
                    result.getAverageTurnaroundTime());

            contentStream.beginText();
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText(row);
            contentStream.endText();
        }
    }

    private static BufferedImage createComparisonChart(Map<String, SchedulingResult> results) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<String, SchedulingResult> entry : results.entrySet()) {
            String algorithm = entry.getKey();
            dataset.addValue(entry.getValue().getAverageWaitingTime(), "Average Waiting Time", algorithm);
            dataset.addValue(entry.getValue().getAverageTurnaroundTime(), "Average Turnaround Time", algorithm);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Algorithm Comparison",
                "Algorithm",
                "Time (ms)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        return chart.createBufferedImage(500, 300);
    }
}
