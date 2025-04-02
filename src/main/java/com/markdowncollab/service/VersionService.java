package com.markdowncollab.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.markdowncollab.exception.DocumentNotFoundException;
import com.markdowncollab.exception.UserNotFoundException;
import com.markdowncollab.exception.VersionNotFoundException;
import com.markdowncollab.model.Document;
import com.markdowncollab.model.User;
import com.markdowncollab.model.Version;
import com.markdowncollab.repository.DocumentRepository;
import com.markdowncollab.repository.UserRepository;
import com.markdowncollab.repository.VersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VersionService {

    private final VersionRepository versionRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    @Autowired
    public VersionService(
            VersionRepository versionRepository,
            DocumentRepository documentRepository,
            UserRepository userRepository) {
        this.versionRepository = versionRepository;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Version createVersion(Long documentId, String content, String description, Long userId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found"));
                
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        Version version = new Version();
        version.setDocument(document);
        version.setAuthor(user);
        version.setContent(content);
        version.setDescription(description);
        version.setCreatedAt(LocalDateTime.now());
        
        return versionRepository.save(version);
    }

    public List<Version> getDocumentVersions(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found"));
                
        return versionRepository.findByDocumentOrderByCreatedAtDesc(document);
    }

    public Optional<Version> getVersion(Long versionId) {
        return versionRepository.findById(versionId);
    }
    
    @Transactional
    public Document restoreVersion(Long versionId, Long userId) {
        Version version = versionRepository.findById(versionId)
                .orElseThrow(() -> new VersionNotFoundException("Version not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        Document document = version.getDocument();
        document.setContent(version.getContent());
        
        // Save the document with the restored content
        Document savedDocument = documentRepository.save(document);
        
        // Create a new version to mark the restoration
        Version newVersion = new Version();
        newVersion.setDocument(document);
        newVersion.setAuthor(user);
        newVersion.setContent(version.getContent());
        newVersion.setDescription("Restored from version created at " + version.getCreatedAt());
        newVersion.setCreatedAt(LocalDateTime.now());
        versionRepository.save(newVersion);
        
        return savedDocument;
    }
}