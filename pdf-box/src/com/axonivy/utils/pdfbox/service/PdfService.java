package com.axonivy.utils.pdfbox.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.rendering.PDFRenderer;

import ch.ivyteam.ivy.environment.Ivy;

public class PdfService {
  private static final int DEFAULT_DPI = 150;
  private static final String SEPARATED_IMAGE_PATTERN = "page_%03d.%s";

  public static byte[] createZippedImagesFromPdf(byte[] bytes, String imageFormat, int dpi) {
    int useDpi = dpi <= 0 ? DEFAULT_DPI : dpi;
    try (PDDocument document = Loader.loadPDF(bytes);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos)) {
      PDFRenderer pdfRenderer = new PDFRenderer(document);
      for (int i = 0; i < document.getNumberOfPages(); i++) {
        BufferedImage image = pdfRenderer.renderImageWithDPI(i, useDpi);
        String fileName = String.format(SEPARATED_IMAGE_PATTERN, i + 1, imageFormat);
        addToZip(zos, fileName, image, imageFormat);
      }
      zos.finish();
      return baos.toByteArray();
    } catch (IOException e) {
      Ivy.log().warn("Can not create Zip from current file", e);
      return new byte[0];
    }
  }

  private static void addToZip(ZipOutputStream zos, String fileName, BufferedImage image, String format)
      throws IOException {
    ZipEntry entry = new ZipEntry(fileName);
    zos.putNextEntry(entry);
    ImageIO.write(image, format, zos);
    zos.closeEntry();
  }

  public static void fillAcroForm(PDDocument document, Map<String, String> data) throws IOException {
    PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
    if (acroForm == null) {
      return;
    }

    acroForm.setNeedAppearances(true);
    for (Map.Entry<String, String> entry : data.entrySet()) {
      PDField field = acroForm.getField(entry.getKey());
      if (field != null) {
        field.setValue(entry.getValue());
      }
    }
  }
}
