package com.axonivy.utils.pdfbox.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.junit.jupiter.api.Test;

import com.axonivy.utils.pdfbox.service.PdfService;

public class PdfServiceTest {

  @Test
  public void testCreateZippedImagesFromPdf() throws IOException {
    byte[] pdfBytes = loadDemoPdf();
    byte[] result = PdfService.createZippedImagesFromPdf(pdfBytes, "png", 150);
    assertNotNull(result);
    assertNotEquals(0, result.length);
    verifyZipContents(result, 1, "png");
    result = PdfService.createZippedImagesFromPdf(pdfBytes, "jpg", 300);
    assertNotNull(result);
    assertNotEquals(0, result.length);
    verifyZipContents(result, 1, "jpg");

    result = PdfService.createZippedImagesFromPdf(pdfBytes, "png", -1);
    assertNotNull(result);
    assertNotEquals(0, result.length);
    verifyZipContents(result, 1, "png");
  }

  @Test
  public void testFillAcroForm() throws IOException {
    PDDocument document = createPdfWithFormFields(new String[] { "firstName", "lastName", "email" });

    Map<String, String> data = new HashMap<>();
    data.put("firstName", "John");
    data.put("lastName", "Doe");
    data.put("email", "john.doe@example.com");
    PdfService.fillAcroForm(document, data);
    PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
    assertEquals("John", acroForm.getField("firstName").getValueAsString());
    assertEquals("Doe", acroForm.getField("lastName").getValueAsString());
    assertEquals("john.doe@example.com", acroForm.getField("email").getValueAsString());
    document.close();

    document = createPdfWithFormFields(new String[] { "firstName", "lastName", "email" });
    data = new HashMap<>();
    data.put("firstName", "Jane");
    PdfService.fillAcroForm(document, data);
    acroForm = document.getDocumentCatalog().getAcroForm();
    assertEquals("Jane", acroForm.getField("firstName").getValueAsString());
    assertEquals("", acroForm.getField("lastName").getValueAsString());
    document.close();

    PDDocument doc = createPdfWithFormFields(new String[] { "firstName", "lastName" });
    assertDoesNotThrow(() -> PdfService.fillAcroForm(doc, new HashMap<>()));
    doc.close();

    PDDocument doc2 = createPdfWithFormFields(new String[] { "field1" });
    assertDoesNotThrow(() -> PdfService.fillAcroForm(doc2, new HashMap<>()));
    doc2.close();
  }

  private byte[] loadDemoPdf() throws IOException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    try (InputStream is = classLoader.getResourceAsStream("pdfform.pdf")) {
      if (is == null) {
        throw new IOException("Demo PDF file not found: pdfform.pdf");
      }
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int len;
      while ((len = is.read(buffer)) != -1) {
        baos.write(buffer, 0, len);
      }
      return baos.toByteArray();
    }
  }

  private PDDocument createPdfWithFormFields(String[] fieldNames) throws IOException {
    PDDocument document = new PDDocument();
    PDPage page = new PDPage(PDRectangle.A4);
    document.addPage(page);
    PDAcroForm acroForm = new PDAcroForm(document);
    document.getDocumentCatalog().setAcroForm(acroForm);
    for (String fieldName : fieldNames) {
      PDTextField textField = new PDTextField(acroForm);
      textField.setPartialName(fieldName);
      acroForm.getFields().add(textField);
    }
    return document;
  }

  private void verifyZipContents(byte[] zipBytes, int expectedPageCount, String format) throws IOException {
    ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes));
    int fileCount = 0;
    ZipEntry entry;
    while ((entry = zis.getNextEntry()) != null) {
      fileCount++;
      assertTrue(entry.getName().matches("page_\\d{3}\\." + format));
    }
    zis.close();
    assertEquals(expectedPageCount, fileCount);
  }
}
