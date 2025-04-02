package com.markdowncollab.pattern.factory;

import java.rmi.server.ExportException;
import com.markdowncollab.model.Document;

/**
 * Interface for document exporters (Factory Pattern product).
 */
public interface DocumentExporter {
    /**
     * Export a document to a specific format
     * 
     * @param document The document to export
     * @return The exported document as a byte array
     * @throws ExportException If export fails
     */
    byte[] export(Document document) throws ExportException;
    
    /**
     * Get the content type of the exported document
     * 
     * @return The content type string
     */
    String getContentType();
    
    /**
     * Get the file extension for the exported document
     * 
     * @return The file extension (without the dot)
     */
    String getFileExtension();
}