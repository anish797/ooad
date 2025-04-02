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
            throw new ExportException("Failed to export document to PDF", e);
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
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>" + title + "</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; margin: 2cm; }\n" +
                "        pre { background-color: #f0f0f0; padding: 10px; border-radius: 5px; }\n" +
                "        code { font-family: monospace; }\n" +
                "        h1, h2, h3, h4, h5, h6 { color: #333; }\n" +
                "        a { color: #0066cc; }\n" +
                "        img { max-width: 100%; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                content +
                "</body>\n" +
                "</html>";
    }
}