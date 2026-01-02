package com.axonivy.utils.pdfbox.demo.managedBean;

import static com.axonivy.utils.pdfbox.demo.constants.PdfBoxConstants.APPLICATION_PDF_MEDIA_TYPE;
import static com.axonivy.utils.pdfbox.demo.constants.PdfBoxConstants.APPLICATION_ZIP_MEDIA_TYPE;
import static com.axonivy.utils.pdfbox.demo.constants.PdfBoxConstants.BOOLEAN_VALUE_FALSE;
import static com.axonivy.utils.pdfbox.demo.constants.PdfBoxConstants.BOOLEAN_VALUE_OFF;
import static com.axonivy.utils.pdfbox.demo.constants.PdfBoxConstants.BOOLEAN_VALUE_ON;
import static com.axonivy.utils.pdfbox.demo.constants.PdfBoxConstants.BOOLEAN_VALUE_TRUE;
import static com.axonivy.utils.pdfbox.demo.constants.PdfBoxConstants.DATA_FILLED_PREFIX_PATTERN;
import static com.axonivy.utils.pdfbox.demo.constants.PdfBoxConstants.DEFAULT_ZIP_NAME;
import static com.axonivy.utils.pdfbox.demo.constants.PdfBoxConstants.FIELD_TYPE_UNKNOWN;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.file.UploadedFile;

import com.axonivy.utils.pdfbox.demo.enums.PdfFieldType;
import com.axonivy.utils.pdfbox.demo.enums.SupportedImageFileExtension;
import com.axonivy.utils.pdfbox.demo.model.FormFieldData;
import com.axonivy.utils.pdfbox.demo.utils.PdfFieldOptionExtractor;
import com.axonivy.utils.pdfbox.service.PdfService;

import ch.ivyteam.ivy.environment.Ivy;

@ManagedBean
@ViewScoped
public class PdfFactoryBean implements Serializable {
  private static final long serialVersionUID = 1L;
  private List<SupportedImageFileExtension> otherDocumentTypes;
  private SupportedImageFileExtension selectedFileExtension;
  private DefaultStreamedContent fileForDownload;
  private UploadedFile uploadedFile;
  private Map<String, String> formData;
  private List<FormFieldData> formFieldDataList;
  private static final int DEFAULT_DPI = 150;
  private String uploadedFileName;

  @PostConstruct
  void init() {
    otherDocumentTypes = Arrays.asList(SupportedImageFileExtension.values());
    selectedFileExtension = SupportedImageFileExtension.PNG;
    if (formData == null) {
      formData = new HashMap<>();
    }
    if (formFieldDataList == null) {
      formFieldDataList = new ArrayList<>();
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
    syncFormData();
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
    formFieldDataList.clear();
    if (uploadedFile == null) {
      return;
    }
    try (PDDocument document = loadDocument()) {
      PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
      if (acroForm != null) {
        for (PDField field : acroForm.getFields()) {
          String fieldType = field.getFieldType();
          int fieldFlags = field.getFieldFlags();
          String fieldTypeLabel = getFieldTypeLabel(fieldType, fieldFlags);
          String originalFieldName = field.getFullyQualifiedName();
          String cleanedFieldName = cleanFieldName(field.getPartialName());
          String fieldValue = field.getValueAsString();
          formData.put(originalFieldName, fieldValue);
          FormFieldData fieldData = new FormFieldData(cleanedFieldName, originalFieldName, fieldValue, fieldType,
              fieldTypeLabel, fieldFlags);
          if (PdfFieldType.CHOICE.getCode().equals(fieldType)) {
            List<String> options = PdfFieldOptionExtractor.extractOptions(field);
            for (String option : options) {
              fieldData.addDropdownOption(option);
            }
          }
          if (PdfFieldType.BUTTON.getCode().equals(fieldType) && fieldData.isCheckbox()) {
            String exportValue = COSName.YES.getName();
            fieldData.setCheckboxExportValue(exportValue);
          }
          formFieldDataList.add(fieldData);
        }
      }
    } catch (IOException e) {
      Ivy.log().error("Error reading form fields", e);
    }
  }

  private String getFieldTypeLabel(String fieldType, int fieldFlags) {
    PdfFieldType type = PdfFieldType.fromCode(fieldType);
    if (type == null) {
      return FIELD_TYPE_UNKNOWN;
    }
    return type.getLabel(fieldFlags);
  }

  private String cleanFieldName(String fieldName) {
    if (fieldName == null || fieldName.isEmpty()) {
      return fieldName;
    }
    String cleaned = fieldName.replaceAll(
        "\\s+(Text Box|Text Field|Checkbox|Check Box|Radio Button|Dropdown|List Box|Signature|Sig)\\s*$", "").trim();
    return cleaned;
  }

  private void syncFormData() {
    for (FormFieldData fieldData : formFieldDataList) {
      String value = fieldData.getFieldValue();

      if (fieldData.isCheckbox()) {
        String exportValue = fieldData.getCheckboxExportValue();
        if (exportValue == null) {
          exportValue = COSName.YES.getName();
        }
        if (value == null || value.isEmpty() || BOOLEAN_VALUE_FALSE.equalsIgnoreCase(value)
            || BOOLEAN_VALUE_OFF.equalsIgnoreCase(value)) {
          value = COSName.Off.getName();
        } else if (BOOLEAN_VALUE_TRUE.equalsIgnoreCase(value) || BOOLEAN_VALUE_ON.equalsIgnoreCase(value)) {
          value = exportValue;
        } else if (value.equalsIgnoreCase(exportValue)) {
          value = exportValue;
        } else {
          value = COSName.Off.getName().equalsIgnoreCase(value) ? COSName.Off.getName() : exportValue;
        }
      }

      formData.put(fieldData.getOriginalFieldName(), value);
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

  public List<FormFieldData> getFormFieldDataList() {
    return formFieldDataList;
  }

  public void setFormFieldDataList(List<FormFieldData> formFieldDataList) {
    this.formFieldDataList = formFieldDataList;
  }
}
