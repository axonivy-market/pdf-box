package com.axonivy.utils.pdfbox.demo.managedBean;

import static com.axonivy.utils.pdfbox.demo.constants.PdfBoxConstants.APPLICATION_PDF_MEDIA_TYPE;
import static com.axonivy.utils.pdfbox.demo.constants.PdfBoxConstants.APPLICATION_ZIP_MEDIA_TYPE;
import static com.axonivy.utils.pdfbox.demo.constants.PdfBoxConstants.DATA_FILLED_PREFIX_PATTERN;
import static com.axonivy.utils.pdfbox.demo.constants.PdfBoxConstants.DEFAULT_ZIP_NAME;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
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

import com.axonivy.utils.pdfbox.demo.enums.SupportedImageFileExtension;
import com.axonivy.utils.pdfbox.service.PdfService;

@ManagedBean
@ViewScoped
public class PdfFactoryBean implements Serializable {
  private static final long serialVersionUID = 1L;
  private List<SupportedImageFileExtension> otherDocumentTypes;
  private SupportedImageFileExtension selectedFileExtension;
  private DefaultStreamedContent fileForDownload;
  private UploadedFile uploadedFile;
  private Map<String, String> formData;
  private static final int DEFAULT_DPI = 150;
  private String uploadedFileName;

  @PostConstruct
  void init() {
    otherDocumentTypes = Arrays.asList(SupportedImageFileExtension.values());
    selectedFileExtension = SupportedImageFileExtension.PNG;
    if (formData == null) {
      formData = new HashMap<>();
    }
  }

  public void convertPdfToOtherDocumentTypes() throws IOException {
    if (uploadedFile != null) {
      String format = selectedFileExtension != null ? selectedFileExtension.getExtension()
          : SupportedImageFileExtension.PNG.getExtension();
      fileForDownload = convertPdfToImageZip(uploadedFile, format, DEFAULT_DPI);
    }
  }

  public void handleFileUpload(FileUploadEvent event) {
    uploadedFile = event.getFile();
    uploadedFileName = uploadedFile.getFileName();
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

  public List<SupportedImageFileExtension> getOtherDocumentTypes() {
    return otherDocumentTypes;
  }

  public void setOtherDocumentTypes(List<SupportedImageFileExtension> otherDocumentTypes) {
    this.otherDocumentTypes = otherDocumentTypes;
  }

  public SupportedImageFileExtension getSelectedFileExtension() {
    return selectedFileExtension;
  }

  public void setSelectedFileExtension(SupportedImageFileExtension selectedFileExtension) {
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

  public String getUploadedFileName() {
    return uploadedFileName;
  }

  public void setUploadedFileName(String uploadedFileName) {
    this.uploadedFileName = uploadedFileName;
  }
}
