package com.markdowncollab.controller;

import java.util.List;
import com.markdowncollab.dto.DocumentDTO;
import com.markdowncollab.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    @Autowired
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    public ResponseEntity<List<DocumentDTO>> getUserDocuments() {
        List<DocumentDTO> documents = documentService.getUserDocuments();
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDTO> getDocument(@PathVariable Long id) {
        DocumentDTO document = documentService.getDocumentById(id);
        return ResponseEntity.ok(document);
    }

    @PostMapping
    public ResponseEntity<Long> createDocument(@RequestParam String title, @RequestParam(required = false) String content) {
        Long documentId = documentService.createDocument(title, content != null ? content : "");
        return ResponseEntity.ok(documentId);
    }

    // Regular update without version creation (for autosaves)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDocument(@PathVariable Long id, @RequestParam String content) {
        documentService.updateDocument(id, content);
        return ResponseEntity.ok().build();
    }
    
    // Explicit save with version creation
    @PostMapping("/{id}/save")
    public ResponseEntity<?> saveDocumentWithVersion(
            @PathVariable Long id, 
            @RequestParam String content,
            @RequestParam String versionDescription) {
        
        documentService.saveDocumentWithVersion(id, content, versionDescription);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/collaborators")
    public ResponseEntity<?> addCollaborator(@PathVariable Long id, @RequestParam String username) {
        boolean added = documentService.addCollaborator(id, username);
        if (added) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().body("User is already a collaborator");
        }
    }

    @DeleteMapping("/{id}/collaborators/{userId}")
    public ResponseEntity<?> removeCollaborator(@PathVariable Long id, @PathVariable Long userId) {
        boolean removed = documentService.removeCollaborator(id, userId);
        if (removed) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().body("User is not a collaborator");
        }
    }

    @PostMapping("/{id}/preview")
    public ResponseEntity<String> previewMarkdown(@RequestParam String content) {
        String html = documentService.renderMarkdown(content);
        return ResponseEntity.ok(html);
    }

    @GetMapping("/{id}/export/{format}")
    public ResponseEntity<byte[]> exportDocument(@PathVariable Long id, @PathVariable String format) {
        try {
            byte[] content = documentService.exportDocument(id, format);
            
            HttpHeaders headers = new HttpHeaders();
            MediaType mediaType;
            String fileName;
            
            switch (format.toLowerCase()) {
                case "pdf":
                    mediaType = MediaType.APPLICATION_PDF;
                    fileName = "document.pdf";
                    break;
                case "html":
                    mediaType = MediaType.TEXT_HTML;
                    fileName = "document.html";
                    break;
                case "docx":
                    mediaType = MediaType.APPLICATION_OCTET_STREAM;
                    fileName = "document.docx";
                    break;
                default:
                    mediaType = MediaType.TEXT_PLAIN;
                    fileName = "document.txt";
            }
            
            headers.setContentType(mediaType);
            headers.setContentDispositionFormData("attachment", fileName);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}