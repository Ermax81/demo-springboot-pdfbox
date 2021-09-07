package com.example.demospringbootpdfbox;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

public class FirstExampleTest {

    @Test
    void updatePdf() {
        String fileName = "tmp.pdf";
        String outputFileName = "pdf-java-output.pdf";
        String imageFileName = "image_600x600.jpg";

        File file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource(fileName)).getFile());

        try {
            PDDocument pDDocument = PDDocument.load(file);
            PDAcroForm pDAcroForm = pDDocument.getDocumentCatalog().getAcroForm();

            // text_1
            // insert text in TextField
            PDField field = pDAcroForm.getField("text_1");
            if (field==null) {
                System.out.println("Field not found");
            } else {
                field.setValue("This is a first field printed by Java");
            }

            // text_2
            // insert text in TextField
            field = pDAcroForm.getField("text_2");
            if (field!=null) {
                field.setValue("This is a second field printed by Java");
            }

            // checkbox_1
            // check checkbox
            // Source: https://stackoverflow.com/questions/14602821/how-to-check-a-check-box-in-pdf-form-using-java-pdfbox-api
            field = pDAcroForm.getField("checkbox_1");
            if (field!=null) {
                ((PDCheckBox) field).check();
            }

            // picto_1
            // insert image in textfield 'area'
            field = pDAcroForm.getField("picto_1");
            PDImageXObject pdImage = PDImageXObject.createFromFile(Objects.requireNonNull(getClass().getClassLoader().getResource(imageFileName)).getPath(), pDDocument);
            if (field!=null) {
                // source: https://stackoverflow.com/questions/46799087/how-to-insert-image-programmatically-in-to-acroform-field-using-java-pdfbox

                // getFieldArea (PDRectangle)
                COSDictionary fieldDict = field.getCOSObject();
                COSArray fieldAreaArray = (COSArray) fieldDict.getDictionaryObject(COSName.RECT);

                PDRectangle rectangle = new PDRectangle(fieldAreaArray);
                float size = rectangle.getHeight();
                float x = rectangle.getLowerLeftX();
                float y = rectangle.getLowerLeftY();

                try (PDPageContentStream contentStream = new PDPageContentStream(pDDocument,
                        pDDocument.getPage(0), PDPageContentStream.AppendMode.APPEND, true)) {
                    contentStream.drawImage(pdImage, x, y, size, size);
                }
            }

            pDDocument.save(new File("src/test/resources/"+outputFileName));
            pDDocument.close();
        } catch(IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    void mergePdf() throws FileNotFoundException {
        String fileOneName = "tmp.pdf";
        String fileTwoName = "pdf-java-output.pdf";
        String fileOutputName = "pdf-merged.pdf";

        // Merge 2 pdf files
        PDFMergerUtility PDFmerger = new PDFMergerUtility();
        PDFmerger.setDestinationFileName("src/test/resources/"+fileOutputName);
        PDFmerger.addSource(new File("src/test/resources/"+fileOneName));
        PDFmerger.addSource(new File("src/test/resources/"+fileTwoName));

        MemoryUsageSetting inMemory = MemoryUsageSetting.setupMainMemoryOnly();
        //MemoryUsageSetting tmpFile = MemoryUsageSetting.setupTempFileOnly();
        try {
            PDFmerger.mergeDocuments(inMemory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
