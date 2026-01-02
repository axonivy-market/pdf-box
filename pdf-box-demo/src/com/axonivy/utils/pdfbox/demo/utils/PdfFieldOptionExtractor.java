package com.axonivy.utils.pdfbox.demo.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.interactive.form.PDChoice;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import ch.ivyteam.ivy.environment.Ivy;

public class PdfFieldOptionExtractor {

  /**
   * Safely extract options from a PDF choice field (dropdown or list box) Uses
   * PDChoice for direct access to choice field options
   * 
   * @param field the PDF field
   * @return list of options, or empty list if none available
   */
  public static List<String> extractOptions(PDField field) {
    List<String> options = new ArrayList<>();

    if (field == null) {
      return options;
    }
    try {
      // Try direct cast to PDChoice interface
      if (field instanceof PDChoice choiceField) {
        List<String> fieldOptions = choiceField.getOptions();
        if (fieldOptions != null) {
          options.addAll(fieldOptions);
        }
      }
    } catch (Exception e) {
      Ivy.log().debug("Could not extract options from field: " + field.getPartialName());
    }
    return options;
  }
}
