package com.markdowncollab.repository;

import java.util.List;
import com.markdowncollab.model.Document;
import com.markdowncollab.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByOwner(User owner);
    
    @Query("SELECT d FROM Document d WHERE d.owner = :user OR :user MEMBER OF d.collaborators")
    List<Document> findAllAccessibleByUser(@Param("user") User user);
    
    List<Document> findByCollaboratorsContaining(User user);
    
    List<Document> findByTitleContainingIgnoreCase(String searchTerm);
}