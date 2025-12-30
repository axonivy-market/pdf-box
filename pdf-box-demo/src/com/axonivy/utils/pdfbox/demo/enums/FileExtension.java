package com.axonivy.utils.pdfbox.demo.enums;

import java.util.Arrays;
import java.util.List;

public enum FileExtension {
  JPG("jpg"), JPEG("jpeg"), PNG("png");

  private final String extension;

  private FileExtension(String extension) {
    this.extension = extension;
  }

  public String getExtension() {
    return extension;
  }

  public static List<FileExtension> getOtherDocumentTypes() {
    return Arrays.asList(JPG, JPEG, PNG);
  }
}
