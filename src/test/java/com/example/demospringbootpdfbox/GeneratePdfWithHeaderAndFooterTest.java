package com.example.demospringbootpdfbox;

import be.quodlibet.boxable.utils.PDStreamUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class GeneratePdfWithHeaderAndFooterTest {

    private final PDFont FONT = PDType1Font.HELVETICA;
    private final float FONT_SIZE = 12;
    private final float LEADING = -1.5f * FONT_SIZE;

    void addHeader(PDDocument document, String header) throws IOException  {
        // get all number of pages.
        int numberOfPages = document.getNumberOfPages();

        for (int i = 0; i < numberOfPages; i++) {
            PDPage fpage = document.getPage(i);
            PDRectangle pageSize = fpage.getMediaBox();
            float x = pageSize.getLowerLeftX();
            float y = pageSize.getLowerLeftY()+pageSize.getHeight();

            // content stream to write content in pdf page.
            PDPageContentStream contentStream = new PDPageContentStream(document, fpage, PDPageContentStream.AppendMode.APPEND, true);
            PDStreamUtils.write(contentStream, header,
                    PDType1Font.HELVETICA, 10, x, y, new Color(102, 102, 102));//set style and size
            contentStream.close();

        }
    }

    // Source: https://stackoverflow.com/questions/16581471/adding-page-numbers-using-pdfbox
    void addPageNumbers(PDDocument document) throws IOException {
        int page_counter = 1;
        int numberOfPages = document.getNumberOfPages();
        String numberingFormat = "Page {0}/"+numberOfPages;
        int offset_X = 60;
        int offset_Y = 18;
        for(PDPage page : document.getPages()){
            PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, false);
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
            PDRectangle pageSize = page.getMediaBox();
            float x = pageSize.getLowerLeftX();
            float y = pageSize.getLowerLeftY();
            contentStream.newLineAtOffset(x+ pageSize.getWidth()-offset_X, y+offset_Y);
            String text = MessageFormat.format(numberingFormat,page_counter);
            contentStream.showText(text);
            contentStream.endText();
            contentStream.close();
            ++page_counter;
        }
    }

    // Source: https://www.oodlestechnologies.com/blogs/How-to-Add-Footer-on-Each-Page-of-a-PDF-document-without-iText/
    void addFooter(PDDocument document, String footer) throws IOException {
        // get all number of pages.
        int numberOfPages = document.getNumberOfPages();

        for (int i = 0; i < numberOfPages; i++) {
            PDPage fpage = document.getPage(i);

            // content stream to write content in pdf page.
            PDPageContentStream contentStream = new PDPageContentStream(document, fpage, PDPageContentStream.AppendMode.APPEND, true);
            PDStreamUtils.write(contentStream, footer,
                    PDType1Font.HELVETICA, 10, 460, 50, new Color(102, 102, 102));//set style and size
            contentStream.close();

        }
    }

    // source: https://memorynotfound.com/apache-pdfbox-adding-multiline-paragraph/
    private java.util.List<String> parseLines(String text, float width) throws IOException {
        List<String> lines = new ArrayList<String>();
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
    private void addParagraph(PDPageContentStream contentStream, float width, float sx,
                              float sy, String text) throws IOException {
        addParagraph(contentStream, width, sx, sy, text, false);
    }

    // source: https://memorynotfound.com/apache-pdfbox-adding-multiline-paragraph/
    private void addParagraph(PDPageContentStream contentStream, float width, float sx,
                              float sy, String text, boolean justify) throws IOException {
        List<String> lines = parseLines(text, width);
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

    // source: https://memorynotfound.com/apache-pdfbox-adding-multiline-paragraph/
    void createParagraph(PDDocument document) {
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

            String text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt" +
                    " ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco" +
                    " laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in " +
                    " ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco" +
                    " laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in " +
                    "voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat" +
                    " non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

            contentStream.beginText();
            //NB: addParagraph retourne le nombre de ligne
            // si nombre de ligne inf ou egale a 32 alors on ajoute sinon on cree une nouvelle page et on recommence
            // TODO extraire   List<String> lines = parseLines(text, width) de addParagraph
            addParagraph(contentStream, width, startX, startY, text, true);
            addParagraph(contentStream, width, 0, -FONT_SIZE, text);
            addParagraph(contentStream, width, 0, -FONT_SIZE, text, false);
            addParagraph(contentStream, width, 0, -FONT_SIZE, text, false);
            addParagraph(contentStream, width, 0, -FONT_SIZE, text, false);
            addParagraph(contentStream, width, 0, -FONT_SIZE, text, false);
            contentStream.endText();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void createPdf() {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(new PDRectangle(PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight()));
            document.addPage(page);

            createParagraph(document);

            addHeader(document, "header to write");
            addPageNumbers(document);
            addFooter(document, "footer to write");

            document.save("src/test/resources/pdf-output-with-header-and-footer.pdf");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
