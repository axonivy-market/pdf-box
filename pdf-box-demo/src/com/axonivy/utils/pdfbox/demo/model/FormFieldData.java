package com.axonivy.utils.pdfbox.demo.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FormFieldData implements Serializable {
  private static final long serialVersionUID = 1L;

  private String fieldName;
  private String originalFieldName;
  private String fieldValue;
  private String fieldType;
  private String fieldTypeLabel;
  private int fieldFlags;
  private List<String> dropdownOptions;
  private String checkboxExportValue;

  public FormFieldData() {
    this.dropdownOptions = new ArrayList<>();
  }

  public FormFieldData(String fieldName, String fieldValue, String fieldType, String fieldTypeLabel, int fieldFlags) {
    this.fieldName = fieldName;
    this.originalFieldName = fieldName;
    this.fieldValue = fieldValue;
    this.fieldType = fieldType;
    this.fieldTypeLabel = fieldTypeLabel;
    this.fieldFlags = fieldFlags;
    this.dropdownOptions = new ArrayList<>();
  }

  public FormFieldData(String fieldName, String originalFieldName, String fieldValue, String fieldType, String fieldTypeLabel, int fieldFlags) {
    this.fieldName = fieldName;
    this.originalFieldName = originalFieldName;
    this.fieldValue = fieldValue;
    this.fieldType = fieldType;
    this.fieldTypeLabel = fieldTypeLabel;
    this.fieldFlags = fieldFlags;
    this.dropdownOptions = new ArrayList<>();
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public String getOriginalFieldName() {
    return originalFieldName;
  }

  public void setOriginalFieldName(String originalFieldName) {
    this.originalFieldName = originalFieldName;
  }

  public String getFieldValue() {
    return fieldValue;
  }

  public void setFieldValue(String fieldValue) {
    this.fieldValue = fieldValue;
  }

  public String getFieldType() {
    return fieldType;
  }

  public void setFieldType(String fieldType) {
    this.fieldType = fieldType;
  }

  public String getFieldTypeLabel() {
    return fieldTypeLabel;
  }

  public void setFieldTypeLabel(String fieldTypeLabel) {
    this.fieldTypeLabel = fieldTypeLabel;
  }

  public int getFieldFlags() {
    return fieldFlags;
  }

  public void setFieldFlags(int fieldFlags) {
    this.fieldFlags = fieldFlags;
  }

  public boolean isCheckbox() {
    return "Btn".equals(fieldType) && (fieldFlags & 0x8000) == 0;
  }

  public boolean isRadioButton() {
    return "Btn".equals(fieldType) && (fieldFlags & 0x8000) == 0x8000;
  }

  public boolean isDropdown() {
    return "Ch".equals(fieldType) && (fieldFlags & 0x20000) == 0x20000;
  }

  public boolean isListBox() {
    return "Ch".equals(fieldType) && (fieldFlags & 0x20000) == 0;
  }

  public boolean isTextField() {
    return "Tx".equals(fieldType);
  }

  public boolean isSignatureField() {
    return "Sig".equals(fieldType);
  }

  public List<String> getDropdownOptions() {
    return dropdownOptions;
  }

  public void setDropdownOptions(List<String> dropdownOptions) {
    this.dropdownOptions = dropdownOptions;
  }

  public void addDropdownOption(String option) {
    this.dropdownOptions.add(option);
  }

  public String getCheckboxExportValue() {
    return checkboxExportValue;
  }

  public void setCheckboxExportValue(String checkboxExportValue) {
    this.checkboxExportValue = checkboxExportValue;
  }
}
