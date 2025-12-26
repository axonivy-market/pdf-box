package com.axonivy.utils.pdfbox.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * Converter class for transforming PDF documents to images. Provides a fluent
 * interface for specifying output format and retrieving results.
 */
public class DocumentConverter {

  private final String DEFAULT_DOCUMENT_NAME = "Document.pdf";
  private final Object source;
  private BufferedImage[] images;

  /**
   * Constructs a DocumentConverter with a PDF source
   * 
   * @param source can be File, Path, or InputStream containing PDF data
   */
  public DocumentConverter(Object source) {
    this.source = source;
  }

  /**
   * Converts the PDF to image format (prepares for output operations)
   * 
   * @return an ImageConverter for retrieving the converted images
   */
  public ImageConverter toImage() {
    return toImage(1.0f); // default DPI scale
  }

  /**
   * Converts the PDF to image format with custom DPI scaling
   * 
   * @param dpiScale the scale factor for DPI (1.0f = 96 DPI, 2.0f = 192 DPI,
   *                 etc.)
   * @return an ImageConverter for retrieving the converted images
   */
  public ImageConverter toImage(float dpiScale) {
    try {
      PDDocument document = loadPDFDocument();
      PDFRenderer pdfRenderer = new PDFRenderer(document);
      int numberOfPages = document.getNumberOfPages();
      this.images = new BufferedImage[numberOfPages];
      for (int i = 0; i < numberOfPages; i++) {
        this.images[i] = pdfRenderer.renderImage(i, dpiScale);
      }
      document.close();
      return new ImageConverter(this.images);
    } catch (IOException e) {
      throw new RuntimeException("Failed to convert PDF to image", e);
    }
  }

  /**
   * Loads the PDF document from the configured source
   * 
   * @return the loaded PDDocument
   * @throws IOException if the PDF cannot be read
   */
  private PDDocument loadPDFDocument() throws IOException {
    return switch (source) {
    case File file -> Loader.loadPDF(file);
    case InputStream inputStream -> Loader.loadPDF(inputStream.readAllBytes());
    case Path path -> Loader.loadPDF(path.toFile());
    case byte[] byteArray -> Loader.loadPDF(byteArray);
    case null, default -> Loader.loadPDF(new File(DEFAULT_DOCUMENT_NAME));
    };
  }

  /**
   * Inner class for handling image output operations
   */
  public static class ImageConverter {
    private final BufferedImage[] images;

    public ImageConverter(BufferedImage[] images) {
      this.images = images;
    }

    /**
     * Returns the converted image(s) as byte array
     * 
     * @return byte array of the first page as PNG
     */
    public byte[] asBytes() {
      return asBytes(0, "png"); // default: first page as PNG
    }

    /**
     * Returns a specific page as byte array
     * 
     * @param pageIndex the page index (0-based)
     * @return byte array of the specified page as PNG
     */
    public byte[] asBytes(int pageIndex) {
      return asBytes(pageIndex, "png");
    }

    /**
     * Returns a specific page as byte array with custom format
     * 
     * @param pageIndex the page index (0-based)
     * @param format    the image format (png, jpg, etc.)
     * @return byte array of the specified page
     */
    public byte[] asBytes(int pageIndex, String format) {
      try {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(images[pageIndex], format, baos);
        return baos.toByteArray();
      } catch (IOException e) {
        throw new RuntimeException("Failed to convert image to bytes", e);
      }
    }

    /**
     * Returns all pages as byte arrays
     * 
     * @return array of byte arrays
     */
    public byte[][] asBytesArray() {
      return asBytesArray("png");
    }

    /**
     * Returns all pages as byte arrays with custom format
     * 
     * @param format the image format (png, jpg, etc.)
     * @return array of byte arrays
     */
    public byte[][] asBytesArray(String format) {
      byte[][] result = new byte[images.length][];
      for (int i = 0; i < images.length; i++) {
        result[i] = asBytes(i, format);
      }
      return result;
    }

    /**
     * Saves the first page as a file
     * 
     * @param outputFile the output file path
     * @return the output file
     */
    public File asFile(File outputFile) {
      return asFile(0, outputFile, "png");
    }

    /**
     * Saves a specific page as a file
     * 
     * @param pageIndex  the page index (0-based)
     * @param outputFile the output file path
     * @return the output file
     */
    public File asFile(int pageIndex, File outputFile) {
      return asFile(pageIndex, outputFile, "png");
    }

    /**
     * Saves a specific page as a file with custom format
     * 
     * @param pageIndex  the page index (0-based)
     * @param outputFile the output file path
     * @param format     the image format (png, jpg, etc.)
     * @return the output file
     */
    public File asFile(int pageIndex, File outputFile, String format) {
      try {
        outputFile.getParentFile().mkdirs();
        ImageIO.write(images[pageIndex], format, outputFile);
        return outputFile;
      } catch (IOException e) {
        throw new RuntimeException("Failed to write image to file", e);
      }
    }

    /**
     * Saves all pages as files with auto-generated names
     * 
     * @param outputDirectory the output directory
     * @param filenamePrefix  the prefix for generated filenames
     * @return array of generated files
     */
    public File[] asFiles(File outputDirectory, String filenamePrefix) {
      return asFiles(outputDirectory, filenamePrefix, "png");
    }

    /**
     * Saves all pages as files with auto-generated names and custom format
     * 
     * @param outputDirectory the output directory
     * @param filenamePrefix  the prefix for generated filenames
     * @param format          the image format (png, jpg, etc.)
     * @return array of generated files
     */
    public File[] asFiles(File outputDirectory, String filenamePrefix, String format) {
      File[] result = new File[images.length];
      outputDirectory.mkdirs();

      for (int i = 0; i < images.length; i++) {
        File file = new File(outputDirectory, filenamePrefix + "_page_" + (i + 1) + "." + format);
        result[i] = asFile(i, file, format);
      }
      return result;
    }

    /**
     * Returns the first page as a Path
     * 
     * @param outputPath the output file path
     * @return the output path
     */
    public Path asPath(Path outputPath) {
      return asPath(0, outputPath, "png");
    }

    /**
     * Returns a specific page as a Path
     * 
     * @param pageIndex  the page index (0-based)
     * @param outputPath the output file path
     * @return the output path
     */
    public Path asPath(int pageIndex, Path outputPath) {
      return asPath(pageIndex, outputPath, "png");
    }

    /**
     * Returns a specific page as a Path with custom format
     * 
     * @param pageIndex  the page index (0-based)
     * @param outputPath the output file path
     * @param format     the image format (png, jpg, etc.)
     * @return the output path
     */
    public Path asPath(int pageIndex, Path outputPath, String format) {
      asFile(pageIndex, outputPath.toFile(), format);
      return outputPath;
    }

    /**
     * Returns the first page as an InputStream
     * 
     * @return InputStream of the image data
     */
    public InputStream asStream() {
      return asStream(0, "png");
    }

    /**
     * Returns a specific page as an InputStream
     * 
     * @param pageIndex the page index (0-based)
     * @return InputStream of the image data
     */
    public InputStream asStream(int pageIndex) {
      return asStream(pageIndex, "png");
    }

    /**
     * Returns a specific page as an InputStream with custom format
     * 
     * @param pageIndex the page index (0-based)
     * @param format    the image format (png, jpg, etc.)
     * @return InputStream of the image data
     */
    public InputStream asStream(int pageIndex, String format) {
      byte[] bytes = asBytes(pageIndex, format);
      return new java.io.ByteArrayInputStream(bytes);
    }

    /**
     * Gets the number of pages in the converted document
     * 
     * @return number of pages
     */
    public int getPageCount() {
      return images.length;
    }

    /**
     * Gets a specific page as BufferedImage
     * 
     * @param pageIndex the page index (0-based)
     * @return the BufferedImage for the page
     */
    public BufferedImage getImage(int pageIndex) {
      if (pageIndex < 0 || pageIndex >= images.length) {
        throw new IndexOutOfBoundsException("Page index out of range: " + pageIndex);
      }
      return images[pageIndex];
    }

    /**
     * Gets all pages as BufferedImage array
     * 
     * @return array of BufferedImages
     */
    public BufferedImage[] getAllImages() {
      return images;
    }
  }

}
