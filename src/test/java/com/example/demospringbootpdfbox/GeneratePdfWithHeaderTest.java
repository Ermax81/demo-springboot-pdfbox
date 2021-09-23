package com.example.demospringbootpdfbox;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class GeneratePdfWithHeaderTest {

    @Test
    void addHeader() {
        try (PDDocument document = new PDDocument()) {
            final PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

                PDFont font = PDType1Font.HELVETICA;
                float fontSize = 12;
                float leading = 1.5f*fontSize;

                PDRectangle mediabox = page.getMediaBox();
                System.out.println("mediabox width:"+mediabox.getWidth());
                System.out.println("mediabox height:"+mediabox.getHeight());
                float margin = 25; //75
                float width = mediabox.getWidth() - 2*margin;
                float startX = mediabox.getLowerLeftX() + margin;
                float startY = mediabox.getUpperRightY() - margin;
                float yOffset = startY;

                contentStream.beginText();
                contentStream.setFont(font, 14);
                contentStream.newLineAtOffset(startX, startY);
                yOffset-=leading;
                contentStream.showText("Voici un exemple de header");
                contentStream.newLineAtOffset(0, -leading);
                yOffset-=leading;

                contentStream.setFont(font, fontSize);
                contentStream.showText("Voici les elements de texte a afficher");

                contentStream.endText();

            }

            document.save("src/test/resources/pdf-output-with-header.pdf");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
