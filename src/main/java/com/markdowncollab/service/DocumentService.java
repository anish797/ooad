package com.markdowncollab.service;

import java.util.List;
import java.util.stream.Collectors;
import com.markdowncollab.dto.DocumentDTO;
import com.markdowncollab.exception.DocumentNotFoundException;
import com.markdowncollab.exception.UserNotFoundException;
import com.markdowncollab.model.Document;
import com.markdowncollab.model.User;
import com.markdowncollab.pattern.factory.DocumentExporter;
import com.markdowncollab.pattern.factory.DocumentExporterFactory;
import com.markdowncollab.pattern.strategy.MarkdownRenderStrategy;
import com.markdowncollab.repository.DocumentRepository;
import com.markdowncollab.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentService {
    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final MarkdownRenderStrategy markdownRenderStrategy;

    @Autowired
    public DocumentService(
            DocumentRepository documentRepository,
            UserRepository userRepository,
            MarkdownRenderStrategy markdownRenderStrategy) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.markdownRenderStrategy = markdownRenderStrategy;
    }
    
    public Document findById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + id));
    }
    
    public DocumentDTO getDocumentById(Long id) {
        Document document = findById(id);
        checkDocumentAccess(document);
        
        // Log access details
        System.out.println("Accessing document - ID: " + id + ", Title: " + document.getTitle());
        
        return new DocumentDTO(document);
    }
        
    public List<DocumentDTO> getUserDocuments() {
        User currentUser = getCurrentUser();
        List<Document> documents = documentRepository.findAllAccessibleByUser(currentUser);
        
        // Log detailed information about accessible documents
        System.out.println("Fetching user documents for user: " + currentUser.getUsername());
        documents.forEach(doc -> 
            System.out.println("Document - ID: " + doc.getId() + ", Title: " + doc.getTitle())
        );
        
        return documents.stream()
                .map(DocumentDTO::new)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public Long createDocument(String title, String content) {
        User currentUser = getCurrentUser();
        
        Document document = new Document();
        document.setTitle(title);
        document.setContent(content);
        document.setOwner(currentUser);
        
        Document savedDocument = documentRepository.save(document);
        return savedDocument.getId();
    }
    
    /**
     * Updates document content without creating a version.
     * Used for auto-saves and routine updates.
     */
    @Transactional
    public void updateDocument(Long id, String content) {
        try {
            Document document = findById(id);
            User currentUser = getCurrentUser();
            
            logger.info("Updating document {} by user {}", id, currentUser.getUsername());
            
            // Log current document state before update
            logger.debug("Before update - Document content: {}", document.getContent());
            logger.debug("Incoming content: {}", content);
            
            checkDocumentAccess(document);
            
            // Update content
            document.setContent(content);
            documentRepository.save(document);
            
            logger.info("Document {} updated successfully", id);
        } catch (Exception e) {
            logger.error("Error updating document", e);
            throw e;
        }
    }
    
    /**
     * Saves document content and creates a version.
     * Used for explicit saves when user requests a save operation.
     */
    @Transactional
    public void saveDocumentWithVersion(Long id, String content, String versionDescription) {
        try {
            Document document = findById(id);
            User currentUser = getCurrentUser();
            
            logger.info("Saving document {} with new version by user {}", id, currentUser.getUsername());
            
            checkDocumentAccess(document);
            
            // Update content
            document.setContent(content);
            
            // Create new version
            document.createNewVersion(content, currentUser, versionDescription);
            
            documentRepository.save(document);
            
            logger.info("Document {} saved with new version successfully", id);
        } catch (Exception e) {
            logger.error("Error saving document with version", e);
            throw e;
        }
    }
    
    @Transactional
    public boolean addCollaborator(Long documentId, String username) {
        Document document = findById(documentId);
        
        // Check if current user is the owner
        User currentUser = getCurrentUser();
        if (!document.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the document owner can add collaborators");
        }
        
        // Find user by username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        
        // Check if user is already a collaborator
        if (document.getCollaborators().contains(user)) {
            return false;
        }
        
        // Add collaborator - Make sure this correctly updates both sides of the relationship
        document.addCollaborator(user);
        
        // Save the document to persist the changes
        documentRepository.save(document);
        
        System.out.println("Added " + user.getUsername() + " as collaborator to document ID: " + 
                        documentId + " with title: " + document.getTitle());
        System.out.println("Document now has " + document.getCollaborators().size() + " collaborators");
        
        return true;
    }
    
    @Transactional
    public boolean removeCollaborator(Long documentId, Long userId) {
        Document document = findById(documentId);
        
        // Check if current user is the owner
        User currentUser = getCurrentUser();
        if (!document.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the document owner can remove collaborators");
        }
        
        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        
        // Remove collaborator
        if (document.getCollaborators().contains(user)) {
            document.removeCollaborator(user);
            documentRepository.save(document);
            return true;
        }
        
        return false;
    }
    
    public String renderMarkdown(String markdownContent) {
        return markdownRenderStrategy.render(markdownContent);
    }
    
    public byte[] exportDocument(Long documentId, String format) throws Exception {
        try {
            Document document = findById(documentId);
            User currentUser = getCurrentUser();
            
            logger.info("Exporting document {} in {} format by user {}", 
                documentId, format, currentUser.getUsername());
            
            checkDocumentAccess(document);
            
            DocumentExporter exporter = DocumentExporterFactory.createExporter(format);
            byte[] exportedContent = exporter.export(document);
            
            logger.info("Document {} exported successfully in {} format", documentId, format);
            return exportedContent;
        } catch (Exception e) {
            logger.error("Error exporting document", e);
            throw e;
        }
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null) {
            logger.error("No authentication context found");
            throw new AccessDeniedException("User not authenticated");
        }
        
        String username = authentication.getName();
        
        try {
            return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));
        } catch (UserNotFoundException e) {
            logger.error("User not found: {}", username);
            throw e;
        }
    }
    
    @Transactional(readOnly = true)
    private void checkDocumentAccess(Document document) {
        User currentUser = getCurrentUser();
        boolean isOwner = document.getOwner().getId().equals(currentUser.getId());
        
        // More detailed check for collaborator status
        boolean isCollaborator = false;
        for (User collaborator : document.getCollaborators()) {
            if (collaborator.getId().equals(currentUser.getId())) {
                isCollaborator = true;
                break;
            }
        }
        
        System.out.println("Access check for user: " + currentUser.getUsername() + 
                        " (ID: " + currentUser.getId() + ")");
        System.out.println("Document owner: " + document.getOwner().getUsername() + 
                        " (ID: " + document.getOwner().getId() + ")");
        System.out.println("Is owner: " + isOwner);
        System.out.println("Is collaborator: " + isCollaborator);
        System.out.println("Collaborators: " + document.getCollaborators().stream()
                        .map(u -> u.getUsername() + " (ID: " + u.getId() + ")")
                        .collect(Collectors.joining(", ")));
        
        if (!isOwner && !isCollaborator) {
            throw new AccessDeniedException("You don't have access to this document");
        }
    }
}