package com.axonivy.utils.pdfbox.service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Factory class for creating PDF to image converters.
 * Provides a fluent API for converting PDFs to images with various input/output formats.
 * 
 * Example usage:
 * <pre>
 * byte[] imageBytes = PDFFactory.from(file).toImage().asBytes();
 * File imageFile = PDFFactory.from(path).toImage().asFile(outputFile);
 * InputStream imageStream = PDFFactory.from(inputStream).toImage().asStream();
 * </pre>
 */
public class PDFFactory {

    // Private constructor to prevent instantiation
    private PDFFactory() {
    }

    /**
     * Creates a converter from a File input
     * @param file the PDF file to convert
     * @return DocumentConverter for chaining operations
     */
    public static DocumentConverter from(File file) {
        return new DocumentConverter(file);
    }

    /**
     * Creates a converter from a file path (String)
     * @param filePath the path to the PDF file
     * @return DocumentConverter for chaining operations
     */
    public static DocumentConverter from(String filePath) {
        return new DocumentConverter(new File(filePath));
    }

    /**
     * Creates a converter from a Path object
     * @param path the Path to the PDF file
     * @return DocumentConverter for chaining operations
     */
    public static DocumentConverter from(Path path) {
        return new DocumentConverter(path.toFile());
    }

    /**
     * Creates a converter from an InputStream
     * @param inputStream the input stream containing PDF data
     * @return DocumentConverter for chaining operations
     */
    public static DocumentConverter from(InputStream inputStream) {
        return new DocumentConverter(inputStream);
    }

    /**
     * Creates a converter from a byte array
     * @param byteArray the byte array containing PDF data
     * @return DocumentConverter for chaining operations
     */
    public static DocumentConverter from(byte[] byteArray) {
        return new DocumentConverter(byteArray);
    }

}
