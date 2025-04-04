package com.markdowncollab.pattern.factory;

import java.io.ByteArrayOutputStream;
import java.rmi.server.ExportException;
import com.markdowncollab.model.Document;
import com.markdowncollab.pattern.strategy.CommonMarkRenderer;
import org.xhtmlrenderer.pdf.ITextRenderer;

/**
 * Concrete product implementation for exporting documents to PDF format.
 */
public class PDFExporter implements DocumentExporter {
    @Override
    public byte[] export(Document document) throws ExportException {
        try {
            // Convert Markdown to HTML
            String html = new CommonMarkRenderer().render(document.getContent());
            
            // Wrap in proper HTML structure
            html = wrapHtml(html, document.getTitle());
            
            // Convert HTML to PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);
            
            return outputStream.toByteArray();
        } catch (Exception e) {
            // Log the full stack trace for debugging
            System.err.println("PDF Export Error: " + e.getMessage());
            e.printStackTrace();
            
            throw new ExportException("Failed to export document to PDF: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getContentType() {
        return "application/pdf";
    }
    
    @Override
    public String getFileExtension() {
        return "pdf";
    }
    
    private String wrapHtml(String content, String title) {
        // Escape special characters in the title
        String safeTitle = escapeHtml(title != null ? title : "Untitled Document");
        
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\" />\n" +
                "    <title>" + safeTitle + "</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; margin: 2cm; }\n" +
                "        pre { background-color: #f0f0f0; padding: 10px; border-radius: 5px; overflow: auto; }\n" +
                "        code { font-family: monospace; }\n" +
                "        h1, h2, h3, h4, h5, h6 { color: #333; }\n" +
                "        a { color: #0066cc; }\n" +
                "        img { max-width: 100%; height: auto; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                content +
                "</body>\n" +
                "</html>";
    }
    
    // Helper method to escape HTML special characters
    private String escapeHtml(String input) {
        if (input == null) return "";
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }
}