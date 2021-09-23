package com.example.demospringbootpdfbox;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class PlayWithImageSizeTest {

    @Test
    void updatePdf() {
        String fileName = "picto.pdf";
        String outputFileName = "pdf-picto-output.pdf";
        //String imageFileName = "building.jpg";
        String imageFileName = "building_620x413.jpg";
        //String imageFileName = "building_474x664.jpg";
        //String imageFileName = "building_503x504.png";

        File file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource(fileName)).getFile());

        try {
            PDDocument pDDocument = PDDocument.load(file);
            PDAcroForm pDAcroForm = pDDocument.getDocumentCatalog().getAcroForm();

            // insert date in TextField
            PDField field = pDAcroForm.getField("date");
            if (field!=null) {
                LocalDate currentDate = LocalDate.now();
                field.setValue(currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
            }

            // insert prestation numero in TextField
            field = pDAcroForm.getField("prestation_num");
            if (field!=null) {
                field.setValue("PRESTA_12345");
            }

            // check checkbox
            // Source: https://stackoverflow.com/questions/14602821/how-to-check-a-check-box-in-pdf-form-using-java-pdfbox-api
            field = pDAcroForm.getField("checkbox_3");
            if (field!=null) {
                ((PDCheckBox) field).check();
            }

            // picto_1
            // insert image in textfield 'area'
            // NB: un redimensionnement de l'image s'opere pour rentrer dans le cadre
            field = pDAcroForm.getField("picto_1");
            PDImageXObject pdImage = PDImageXObject.createFromFile(Objects.requireNonNull(getClass().getClassLoader().getResource(imageFileName)).getPath(), pDDocument);

            int imageHeight = pdImage.getHeight();
            int imageWidth = pdImage.getWidth();
            System.out.println("Image Height:"+imageHeight);
            System.out.println("Image Width:"+imageWidth);
            float scale = 1f;

            if (field!=null) {
                // source: https://stackoverflow.com/questions/46799087/how-to-insert-image-programmatically-in-to-acroform-field-using-java-pdfbox

                // getFieldArea (PDRectangle)
                COSDictionary fieldDict = field.getCOSObject();
                COSArray fieldAreaArray = (COSArray) fieldDict.getDictionaryObject(COSName.RECT);

                PDRectangle rectangle = new PDRectangle(fieldAreaArray);
                //float size = rectangle.getHeight();
                float rectangleHeight = rectangle.getHeight();
                float rectangleWidth = rectangle.getWidth();
                System.out.println("rectangle Height:"+rectangleHeight);
                System.out.println("rectangle Width:"+rectangleWidth);

                float x = rectangle.getLowerLeftX();
                float y = rectangle.getLowerLeftY();
                float xOffset = x;
                float yOffset = y;

                if (imageWidth > imageHeight) {
                    scale = rectangleWidth / imageWidth;

                    // y = yOffset : image en bas du carre
                    // yOsset = y + (imageHeight * scale)/2 : image en haut du carre
                    // yOsset = y + (imageHeight * scale)/4 : image au milieu du carre
                    yOffset = y + (imageHeight * scale)/4;
                    rectangleHeight = imageHeight * scale;
                } else if (imageWidth < imageHeight) {
                    scale = rectangleHeight / imageHeight;

                    xOffset = x + (imageWidth * scale)/4;
                    rectangleWidth = imageWidth * scale;
                }
                System.out.println("Scale: "+scale);

                try (PDPageContentStream contentStream = new PDPageContentStream(pDDocument,
                        pDDocument.getPage(0), PDPageContentStream.AppendMode.APPEND, true)) {
                    //contentStream.drawImage(pdImage, x, y, size, size);
                    //contentStream.drawImage(pdImage, x, y, rectangleWidth, rectangleHeight);

                    PDFont font = PDType1Font.HELVETICA;
                    float fontSize = 12f;
                    contentStream.beginText();
                    contentStream.setFont(font,fontSize);
                    contentStream.newLineAtOffset(rectangle.getUpperRightX()-rectangleWidth-20,rectangle.getUpperRightY()+25);
                    contentStream.showText("Below you will find the building you're looking for:");
                    contentStream.endText();

                    contentStream.drawImage(pdImage, xOffset, yOffset, rectangleWidth, rectangleHeight);
                }
            }

            pDDocument.save(new File("src/test/resources/"+outputFileName));
            pDDocument.close();

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

}
