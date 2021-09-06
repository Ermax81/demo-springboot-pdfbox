package com.example.demospringbootpdfbox;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FirstExampleTest {

    @Test
    void updatePdf() {
        String fileName = "tmp.pdf";
        String outputFileName = "pdf-java-output.pdf";
        File file = new File(getClass().getClassLoader().getResource(fileName).getFile());
        if (file == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        }

        try {
            PDDocument pDDocument = PDDocument.load(file);
            PDAcroForm pDAcroForm = pDDocument.getDocumentCatalog().getAcroForm();
            PDField field = pDAcroForm.getField("text_1");
            if (field==null) {
                System.out.println("Field not found");
            }
            field.setValue("This is a first field printed by Java");
            field = pDAcroForm.getField("text_2");
            field.setValue("This is a second field printed by Java");

            pDDocument.save(new File("src/test/resources/"+outputFileName));
            pDDocument.close();
        } catch(IOException e) {
            e.printStackTrace();
        }

    }
}
