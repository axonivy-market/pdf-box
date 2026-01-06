# PDF Box Utils

The **PDF Box Utils** is a powerful utility library for the Axon Ivy platform, built on top of the industry-standard [Apache PDFBox](https://pdfbox.apache.org/). It simplifies complex PDF manipulations, allowing developers to easily integrate PDF processing capabilities into their business processes.

## Key Features

- 🖼️ **PDF to Image Conversion**: Transform PDF pages into high-quality images (PNG, JPG, etc.) with customizable DPI settings.
- 📝 **AcroForm Management**: Programmatically fill and update PDF form fields with dynamic data.
- 📦 **Batch Processing & Zipping**: Automatically package converted images into a single ZIP archive for efficient handling.

## Demo

The PDF Box Utils includes comprehensive demo processes showcasing all available functionality:

### 1. PDF to Image Conversion
Convert your PDF documents into a collection of images in just a few steps:
1. **Upload**: Select your PDF file.
2. **Configure**: Choose your desired image format (e.g., PNG, JPG).
3. **Download**: Click the "Convert And Download" button to receive a ZIP file containing all pages as images.

![Convert to images](images/convert-to-images.png)

### 2. AcroForm Field Updates
Easily manipulate PDF forms:
1. **Upload**: Provide a PDF containing an AcroForm.

![PDF with AcroForm](images/sample-pdf.png)

2. **Edit**: Modify the field values directly in the UI.

![Update AcroForm](images/acro-form.png)

3. **Generate**: Click the "Update And Download" button to get the modified PDF document.

![Updated AcroForm](images/updated-acro-form.png)

