package com.axonivy.utils.pdfbox.demo.enums;

public enum SupportedImageFileExtension {
  JPG("jpg"), JPEG("jpeg"), PNG("png");

  private final String extension;

  private SupportedImageFileExtension(String extension) {
    this.extension = extension;
  }

  public String getExtension() {
    return extension;
  }
}
