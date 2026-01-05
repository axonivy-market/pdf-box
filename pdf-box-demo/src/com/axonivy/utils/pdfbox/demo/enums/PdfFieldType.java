package com.axonivy.utils.pdfbox.demo.enums;

public enum PdfFieldType {
  TEXT("Tx", "Text Field"), CHOICE("Ch", "Choice Field"), BUTTON("Btn", "Button"), SIGNATURE("Sig", "Signature");

  private final String code;
  private final String displayName;

  PdfFieldType(String code, String displayName) {
    this.code = code;
    this.displayName = displayName;
  }

  public String getCode() {
    return code;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getLabel(int fieldFlags) {
    switch (this) {
    case TEXT, SIGNATURE:
      return displayName;
    case CHOICE:
      boolean isDropdown = (fieldFlags & 0x20000) == 0x20000;
      return isDropdown ? "Dropdown" : "List Box";
    case BUTTON:
      boolean isRadioButton = (fieldFlags & 0x8000) == 0x8000;
      return isRadioButton ? "Radio Button" : "Checkbox";
    default:
      return "Unknown";
    }
  }

  public static PdfFieldType fromCode(String code) {
    for (PdfFieldType type : PdfFieldType.values()) {
      if (type.code.equals(code)) {
        return type;
      }
    }
    return null;
  }
}
