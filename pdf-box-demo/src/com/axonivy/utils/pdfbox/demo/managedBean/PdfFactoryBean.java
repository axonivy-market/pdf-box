package com.axonivy.utils.pdfbox.demo.managedBean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.file.UploadedFile;

import com.axonivy.utils.pdfbox.demo.enums.FileExtension;
import com.axonivy.utils.pdfbox.service.PdfService;

@ManagedBean
@ViewScoped
public class PdfFactoryBean implements Serializable {
  private static final long serialVersionUID = 1L;
  private List<FileExtension> otherDocumentTypes;
  private FileExtension selectedFileExtension;
  private DefaultStreamedContent fileForDownload;
  private UploadedFile uploadedFile;
  private Map<String, String> formData;
  private static final int DEFAULT_DPI = 150;
  private final String DEFAULT_IMAGE_FORMAT = FileExtension.PNG.getExtension();
  private final String APPLICATION_PDF_MEDIA_TYPE = "application/pdf";
  private final String APPLICATION_ZIP_MEDIA_TYPE = "application/zip";
  private final String DEFAULT_ZIP_NAME = "pdf_images.zip";
  private final String DATA_FILLED_PREFIX_PATTERN = "filled-%s";

  @PostConstruct
  void init() {
    otherDocumentTypes = FileExtension.getOtherDocumentTypes();
    selectedFileExtension = FileExtension.PNG;
    if (formData == null) {
      formData = new HashMap<>();
    }
  }

  public void convertPdfToOtherDocumentTypes() throws IOException {
    if (uploadedFile != null) {
      String format = selectedFileExtension != null ? selectedFileExtension.getExtension() : DEFAULT_IMAGE_FORMAT;
      fileForDownload = convertPdfToImageZip(uploadedFile, format, DEFAULT_DPI);
    }
  }

  public void handleFileUpload(FileUploadEvent event) {
    this.uploadedFile = event.getFile();
    updateFormData();
  }

  public void updatePdfFormAndDownload() throws IOException {
    validateUpload();
    if (formData.isEmpty()) {
      throw new IllegalStateException("Form data is empty");
    }
    try (PDDocument document = loadDocument(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      PdfService.fillAcroForm(document, formData);
      document.save(baos);
      byte[] pdfBytes = baos.toByteArray();
      fileForDownload = DefaultStreamedContent.builder()
          .name(String.format(DATA_FILLED_PREFIX_PATTERN, uploadedFile.getFileName()))
          .contentType(APPLICATION_PDF_MEDIA_TYPE).stream(() -> new ByteArrayInputStream(pdfBytes)).build();
    }
  }

  public void updateFormData() {
    formData.clear();
    if (uploadedFile == null) {
      return;
    }
    try (PDDocument document = loadDocument()) {
      PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
      if (acroForm != null) {
        for (PDField field : acroForm.getFields()) {
          formData.put(field.getFullyQualifiedName(), field.getValueAsString());
        }
      }
    } catch (IOException e) {
    }
  }

  public DefaultStreamedContent convertPdfToImageZip(UploadedFile file, String imageFormat, int dpi)
      throws IOException {
    if (file == null) {
      throw new IllegalArgumentException("Uploaded file cannot be null");
    }
    return DefaultStreamedContent.builder().name(DEFAULT_ZIP_NAME).contentType(APPLICATION_ZIP_MEDIA_TYPE)
        .stream(() -> new ByteArrayInputStream(
            PdfService.createZippedImagesFromPdf(uploadedFile.getContent(), imageFormat, dpi)))
        .build();
  }

  private PDDocument loadDocument() throws IOException {
    validateUpload();
    return Loader.loadPDF(uploadedFile.getContent());
  }

  private void validateUpload() {
    if (uploadedFile == null) {
      throw new IllegalStateException("No PDF uploaded");
    }
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
