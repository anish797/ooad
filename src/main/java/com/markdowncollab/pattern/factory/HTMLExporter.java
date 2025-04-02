package com.markdowncollab.pattern.factory;

import java.nio.charset.StandardCharsets;
import java.rmi.server.ExportException;
import com.markdowncollab.model.Document;
import com.markdowncollab.pattern.strategy.CommonMarkRenderer;

/**
 * Concrete product implementation for exporting documents to HTML format.
 */
public class HTMLExporter implements DocumentExporter {
    @Override
    public byte[] export(Document document) throws ExportException {
        try {
            // Convert Markdown to HTML
            String html = new CommonMarkRenderer().render(document.getContent());
            
            // Wrap in proper HTML structure
            html = wrapHtml(html, document.getTitle());
            
            return html.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new ExportException("Failed to export document to HTML", e);
        }
    }
    
    @Override
    public String getContentType() {
        return "text/html";
    }
    
    @Override
    public String getFileExtension() {
        return "html";
    }
    
    private String wrapHtml(String content, String title) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>" + title + "</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px; }\n" +
                "        pre { background-color: #f0f0f0; padding: 10px; border-radius: 5px; overflow: auto; }\n" +
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