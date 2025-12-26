package com.axonivy.utils.pdfbox.demo.managedBean;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.faces.bean.ManagedBean;
import javax.faces.view.ViewScoped;
import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.file.UploadedFile;

import com.axonivy.utils.pdfbox.demo.enums.FileExtension;

import ch.ivyteam.ivy.environment.Ivy;

@ManagedBean
@ViewScoped
public class PdfFactoryBean {
  private List<FileExtension> otherDocumentTypes = FileExtension.getOtherDocumentTypes();
  private FileExtension selectedFileExtension = FileExtension.PNG;
  private DefaultStreamedContent fileForDownload;
  private UploadedFile uploadedFile;
  private Map<String, String> formData;
  private PDDocument document;

  public void convertPdfToOtherDocumentTypes() throws IOException {
    if (uploadedFile != null) {
      fileForDownload = convertPdfToImageZip(uploadedFile);
    }
  }

  public void updateFormData(FileUploadEvent event) {
    try {
      document = Loader.loadPDF(event.getFile().getInputStream().readAllBytes());
      formData = new HashMap<String, String>();
      PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
      if (acroForm == null) {
        return;
      }
      for (PDField field : acroForm.getFields()) {
        String fieldName = field.getFullyQualifiedName();
        String fieldValue = field.getValueAsString();
        formData.put(fieldName, fieldValue);
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public DefaultStreamedContent convertPdfToImageZip(UploadedFile uploadedFile, String imageFormat, int dpi)
      throws IOException {
    long start, end, total;
    start = System.currentTimeMillis();
    // Validate inputs
    if (uploadedFile == null) {
      throw new IllegalArgumentException("Uploaded file cannot be null");
    }

    if (imageFormat == null || imageFormat.trim().isEmpty()) {
      imageFormat = "png";
    }

    if (dpi <= 0) {
      dpi = 150;
    }
    int pageCount = 0;
    ByteArrayOutputStream zipOutput = new ByteArrayOutputStream();
    try (ZipOutputStream zipStream = new ZipOutputStream(zipOutput)) {
      try {
        // Create a PDF renderer
        PDFRenderer pdfRenderer = new PDFRenderer(document);

        // Iterate through each page
        pageCount = document.getNumberOfPages();
        for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
          // Render page to image
          BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(pageIndex, dpi);

          // Create ZIP entry for this page
          String imageName = String.format("page_%03d.%s", pageIndex + 1, imageFormat);
          zipStream.putNextEntry(new ZipEntry(imageName));

          // Write image to ZIP
          ImageIO.write(bufferedImage, imageFormat, zipStream);
          zipStream.closeEntry();
        }
      } finally {
        document.close();
      }
    }
    ByteArrayInputStream zipInputStream = new ByteArrayInputStream(zipOutput.toByteArray());
    end = System.currentTimeMillis();
    total = end - start;
    Ivy.log().warn("Total time of processing {0} page(s) with average speed: {1} milisecond(s) - {2}/page", pageCount,
        total, total / pageCount);
    // Prepare the streamed content for download
    return DefaultStreamedContent.builder().name("pdf_images.zip").contentType("application/zip")
        .stream(() -> zipInputStream).build();
  }

  /**
   * Overloaded method with default parameters
   */
  public DefaultStreamedContent convertPdfToImageZip(UploadedFile uploadedFile) throws IOException {
    return convertPdfToImageZip(uploadedFile, "png", 150);
  }

  /**
   * Overloaded method with custom image format
   */
  public DefaultStreamedContent convertPdfToImageZip(UploadedFile uploadedFile, String imageFormat) throws IOException {
    return convertPdfToImageZip(uploadedFile, imageFormat, 150);
  }

  public List<FileExtension> getOtherDocumentTypes() {
    return otherDocumentTypes;
  }

  public void setOtherDocumentTypes(List<FileExtension> otherDocumentTypes) {
    this.otherDocumentTypes = otherDocumentTypes;
  }

  public FileExtension getSelectedFileExtension() {
    return selectedFileExtension;
  }

  public void setSelectedFileExtension(FileExtension selectedFileExtension) {
    this.selectedFileExtension = selectedFileExtension;
  }

  public DefaultStreamedContent getFileForDownload() {
    return fileForDownload;
  }

  public void setFileForDownload(DefaultStreamedContent fileForDownload) {
    this.fileForDownload = fileForDownload;
  }

  public UploadedFile getUploadedFile() {
    return uploadedFile;
  }

  public void setUploadedFile(UploadedFile uploadedFile) {
    this.uploadedFile = uploadedFile;
  }

  public Map<String, String> getFormData() {
    return formData;
  }

  public void setFormData(Map<String, String> formData) {
    this.formData = formData;
  }
}
