package com.markdowncollab.repository;

import java.util.List;
import com.markdowncollab.model.Document;
import com.markdowncollab.model.Version;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VersionRepository extends JpaRepository<Version, Long> {
    List<Version> findByDocumentOrderByCreatedAtDesc(Document document);
    
    List<Version> findByDocumentIdOrderByCreatedAtDesc(Long documentId);
}