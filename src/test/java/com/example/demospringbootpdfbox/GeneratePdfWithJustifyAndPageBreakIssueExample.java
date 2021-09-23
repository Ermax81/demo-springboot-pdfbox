package com.example.demospringbootpdfbox;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// TODO make a loop to test page break
// WORK IN PROGRESS...
public class GeneratePdfWithJustifyAndPageBreakIssueExample {


    private final PDFont FONT = PDType1Font.HELVETICA;
    private final float FONT_SIZE = 12;
    private final float LEADING = -1.5f * FONT_SIZE;

    int nbLineMaxToWrite = 32;

    @Test
    void createPdf() {
        try (PDDocument document = new PDDocument()){

            int nbRemainingLinesToWrite = nbLineMaxToWrite;
            String text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt" +
                    " ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco" +
                    " laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in " +
                    " ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco" +
                    " laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in " +
                    "voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat" +
                    " non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
            for (int i=0; i<10; i++) {
                //nbRemainingLinesToWrite =
                createParagraph(document, text, false, nbRemainingLinesToWrite);
            }
            //nbRemainingLinesToWrite =
            createParagraph(document, text, true, nbRemainingLinesToWrite);
            document.save("src/test/resources/pdf-output-with-justify-and-pagebreak-example.pdf");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // source: https://memorynotfound.com/apache-pdfbox-adding-multiline-paragraph/
    private List<String> parseLines(String text, float width) throws IOException {
        List<String> lines = new ArrayList<>();
        int lastSpace = -1;
        while (text.length() > 0) {
            int spaceIndex = text.indexOf(' ', lastSpace + 1);
            if (spaceIndex < 0)
                spaceIndex = text.length();
            String subString = text.substring(0, spaceIndex);
            float size = FONT_SIZE * FONT.getStringWidth(subString) / 1000;
            if (size > width) {
                if (lastSpace < 0){
                    lastSpace = spaceIndex;
                }
                subString = text.substring(0, lastSpace);
                lines.add(subString);
                text = text.substring(lastSpace).trim();
                lastSpace = -1;
            } else if (spaceIndex == text.length()) {
                lines.add(text);
                text = "";
            } else {
                lastSpace = spaceIndex;
            }
        }
        return lines;
    }

    // source: https://memorynotfound.com/apache-pdfbox-adding-multiline-paragraph/
    private int addParagraph(PDPageContentStream contentStream, float width, float sx,
                             float sy, String text, int nbLineToEnd) throws IOException {
        return addParagraph(contentStream, width, sx, sy, text, false, nbLineToEnd);
    }

    // source: https://memorynotfound.com/apache-pdfbox-adding-multiline-paragraph/
    private int addParagraph(PDPageContentStream contentStream, float width, float sx,
                             float sy, String text, boolean justify, int nbLineToEnd) throws IOException {
        List<String> lines = parseLines(text, width);
        int linesSize = lines.size();
        int nbRemainingLines = (nbLineToEnd>=linesSize)? nbLineToEnd-linesSize : 0;
        if (nbRemainingLines >= 0) {
            contentStream.setFont(FONT, FONT_SIZE);
            contentStream.newLineAtOffset(sx, sy);
            for (String line: lines) {
                float charSpacing = 0;
                if (justify){
                    if (line.length() > 1) {
                        float size = FONT_SIZE * FONT.getStringWidth(line) / 1000;
                        float free = width - size;
                        if (free > 0 && !lines.get(lines.size() - 1).equals(line)) {
                            charSpacing = free / (line.length() - 1);
                        }
                    }
                }
                contentStream.setCharacterSpacing(charSpacing);
                contentStream.showText(line);
                contentStream.newLineAtOffset(0, LEADING); //same as contentStream.setLeading(LEADING); + contentStream.newLine();

            }
        }
        return nbRemainingLines;
    }

    // source: https://memorynotfound.com/apache-pdfbox-adding-multiline-paragraph/
    void createParagraph(PDDocument document, String text, boolean isLastParagraph, int nbRemainingLinesToWrite) {
        PDPage page = new PDPage(new PDRectangle(PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight()));
        document.addPage(page);
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page,
                PDPageContentStream.AppendMode.APPEND, true)) {

            PDRectangle mediaBox = page.getMediaBox();
            float marginY = 80;
            float marginX = 60;
            float width = mediaBox.getWidth() - 2 * marginX;
            float startX = mediaBox.getLowerLeftX() + marginX;
            float startY = mediaBox.getUpperRightY() - marginY;

            contentStream.beginText();
            //NB: addParagraph retourne le nombre de ligne
            // si nombre de ligne inf ou egale a 32 alors on ajoute sinon on cree une nouvelle page et on recommence
            // TODO extract   List<String> lines = parseLines(text, width) from addParagraph
            nbRemainingLinesToWrite = addParagraph(contentStream, width, startX, startY, text, true, nbRemainingLinesToWrite);

            nbRemainingLinesToWrite = addParagraph(contentStream, width, 0, -FONT_SIZE, text, nbRemainingLinesToWrite);
            nbRemainingLinesToWrite = addParagraph(contentStream, width, 0, -FONT_SIZE, text, false, nbRemainingLinesToWrite);
            nbRemainingLinesToWrite = addParagraph(contentStream, width, 0, -FONT_SIZE, text, false, nbRemainingLinesToWrite);
            nbRemainingLinesToWrite = addParagraph(contentStream, width, 0, -FONT_SIZE, text, false, nbRemainingLinesToWrite);
            if (nbRemainingLinesToWrite==0) {
                contentStream.endText();
                page = new PDPage(new PDRectangle(PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight()));
                document.addPage(page);

                try (PDPageContentStream contentStream2 = new PDPageContentStream(document, page,
                        PDPageContentStream.AppendMode.APPEND, true)) {
                    contentStream2.beginText();
                    addParagraph(contentStream2, width, startX, startY, text, true, nbRemainingLinesToWrite);
                    contentStream2.endText();
                }
            }
            //nbRemainingLinesToWrite = addParagraph(contentStream, width, 0, -FONT_SIZE, text, false, nbRemainingLinesToWrite);
            //contentStream.endText();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
