package com.markdowncollab.pattern.factory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.server.ExportException;
import com.markdowncollab.model.Document;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

/**
 * Concrete product implementation for exporting documents to DOCX format.
 */
public class DocxExporter implements DocumentExporter {
    @Override
    public byte[] export(Document document) throws ExportException {
        try (XWPFDocument docx = new XWPFDocument()) {
            // Add title
            XWPFParagraph titleParagraph = docx.createParagraph();
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setBold(true);
            titleRun.setFontSize(16);
            titleRun.setText(document.getTitle());
            titleRun.addBreak();
            
            // Add content (simple implementation - just adds as plain text)
            // A more complete implementation would parse the markdown and format accordingly
            XWPFParagraph contentParagraph = docx.createParagraph();
            XWPFRun contentRun = contentParagraph.createRun();
            contentRun.setText(document.getContent());
            
            // Write to byte array
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            docx.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new ExportException("Failed to export document to DOCX", e);
        }
    }
    
    @Override
    public String getContentType() {
        return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    }
    
    @Override
    public String getFileExtension() {
        return "docx";
    }
}