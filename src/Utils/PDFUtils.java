package Utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.IOException;

public class PDFUtils {

    /**
     * Adds an image centered on the given PDF page.
     *
     * @param document  The PDF document.
     * @param page      The page to which the image will be added.
     * @param imagePath Path to the image file (e.g., PNG).
     * @throws IOException if the image file can't be read or written.
     */
    public static void addImageToPage(PDDocument document, PDPage page, String imagePath) throws IOException {
        PDImageXObject pdImage = PDImageXObject.createFromFile(imagePath, document);

        float scale = 0.5f;
        float imageWidth = pdImage.getWidth() * scale;
        float imageHeight = pdImage.getHeight() * scale;

        float pageWidth = page.getMediaBox().getWidth();
        float pageHeight = page.getMediaBox().getHeight();

        float x = (pageWidth - imageWidth) / 2;
        float y = (pageHeight - imageHeight) / 2;

        try (PDPageContentStream contents = new PDPageContentStream(document, page, 
                PDPageContentStream.AppendMode.APPEND, true)) {
            contents.drawImage(pdImage, x, y, imageWidth, imageHeight);
        }
    }
}
