package com.markdowncollab.pattern.factory;

import com.markdowncollab.exception.UnsupportedExportFormatException;

/**
 * Factory class for creating document exporters based on the requested format.
 */
public class DocumentExporterFactory {
    /**
     * Create an appropriate exporter for the requested format
     * 
     * @param format The export format (pdf, html, docx)
     * @return A DocumentExporter instance for the requested format
     * @throws UnsupportedExportFormatException If the format is not supported
     */
    public static DocumentExporter createExporter(String format) throws UnsupportedExportFormatException {
        switch (format.toLowerCase()) {
            case "pdf":
                return new PDFExporter();
            case "html":
                return new HTMLExporter();
            case "docx":
                return new DocxExporter();
            default:
                throw new UnsupportedExportFormatException("Unsupported export format: " + format);
        }
    }
}