package com.example.demospringbootpdfbox;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.Row;
import be.quodlibet.boxable.line.LineStyle;
import be.quodlibet.boxable.utils.PDStreamUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;
import org.vandeseer.easytable.RepeatedHeaderTableDrawer;
import org.vandeseer.easytable.structure.Table;
import org.vandeseer.easytable.structure.cell.TextCell;

import java.awt.*;
import java.io.IOException;
import java.text.MessageFormat;

public class GeneratePdfWithFooterTest {

    @Test
    public void createPdfDocuments () {

        int margin = 20;
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float yStartNewPage = page.getMediaBox().getHeight() - (4 * margin);
                float tableWidth = page.getMediaBox().getWidth() + (18 * margin);
                boolean drawContent = true;
                float yStart = yStartNewPage;
                float bottomMargin = 70;
                float pageTopMargin = 70;
                String migrantHeading = "Migrant";
                BaseTable table = new BaseTable(yStart, yStartNewPage, pageTopMargin, bottomMargin, tableWidth, margin * 3,
                        document, page, true, drawContent);

                LineStyle lineStyle = new LineStyle(Color.WHITE, 1);
                // add cell in table.
                Row<PDPage> headerRow = table.createRow(30f);
                Cell<PDPage> cell = headerRow.createCell(35, "//Contents in header row");
                cell.setFont(PDType1Font.HELVETICA_BOLD);
                cell.setFontSize(14);
                cell.setLeftPadding(40);
                cell.setTextColor(new Color(28, 69, 135));
                cell.setBorderStyle(lineStyle);

                // add other content to pdf......


                // draw table
                table.draw();
                // call a method to add footer or header
                addFooter(document);
                //addHeader(document, listofcellDetails, listOfCellSize, false, 0);


            }

            RepeatedHeaderTableDrawer.builder()
                    .table(createTable())
                    .startX(50)
                    .startY(100F)
                    .endY(50F) // note: if not set, table is drawn over the end of the page
                    .build()
                    .draw(() -> document, () -> new PDPage(PDRectangle.A4), 50f);

            addPageNumbers(document,"Page {0}",60,18);

            document.save("src/test/resources/pdf-output-with-footer.pdf");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // method to add footer
    // Source: https://www.oodlestechnologies.com/blogs/How-to-Add-Footer-on-Each-Page-of-a-PDF-document-without-iText/
    public void addFooter(PDDocument document) throws IOException {

        // get all number of pages.

        int numberOfPages = document.getNumberOfPages();

        for (int i = 0; i < numberOfPages; i++) {
            PDPage fpage = document.getPage(i);

            // content stream to write content in pdf page.
            PDPageContentStream contentStream = new PDPageContentStream(document, fpage, PDPageContentStream.AppendMode.APPEND, true);
            PDStreamUtils.write(contentStream, "contents to write",
                    PDType1Font.HELVETICA, 10, 460, 50, new Color(102, 102, 102));//set style and size
            contentStream.close();

        }
    }

    // Source: https://stackoverflow.com/questions/16581471/adding-page-numbers-using-pdfbox
    public void addPageNumbers(PDDocument document, String numberingFormat, int offset_X, int offset_Y) throws IOException {
        int page_counter = 1;
        for(PDPage page : document.getPages()){
            PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, false);
            contentStream.beginText();
            contentStream.setFont(PDType1Font.TIMES_ITALIC, 10);
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

    // Source: https://github.com/vandeseer/easytable/blob/master/src/test/java/org/vandeseer/integrationtest/TableOverSeveralPagesTest.java
    private Table createTable() {
        final Table.TableBuilder tableBuilder = Table.builder()
                .addColumnOfWidth(200)
                .addColumnOfWidth(200);

        TextCell dummyHeaderCell = TextCell.builder()
                .text("Header dummy")
                .backgroundColor(Color.BLUE)
                .textColor(Color.WHITE)
                .borderWidth(1F)
                .build();

        tableBuilder.addRow(
                org.vandeseer.easytable.structure.Row.builder()
                        .add(dummyHeaderCell)
                        .add(dummyHeaderCell)
                        .build());

        for (int i = 0; i < 50; i++) {
            tableBuilder.addRow(
                    org.vandeseer.easytable.structure.Row.builder()
                            .add(TextCell.builder()
                                    .text("dummy " + i)
                                    .borderWidth(1F)
                                    .build())
                            .add(TextCell.builder()
                                    .text("dummy " + i)
                                    .borderWidth(1F)
                                    .build())
                            .build());
        }

        return tableBuilder.build();
    }

}
