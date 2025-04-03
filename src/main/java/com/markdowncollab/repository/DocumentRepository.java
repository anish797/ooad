package com.markdowncollab.repository;

import java.util.List;
import java.util.Optional;
import com.markdowncollab.model.Document;
import com.markdowncollab.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    // Add this method to fetch document with collaborators in one query
    @Query("SELECT d FROM Document d LEFT JOIN FETCH d.collaborators WHERE d.id = :id")
    Optional<Document> findByIdWithCollaborators(@Param("id") Long id);
    
    // Update the existing query to fetch collaborators eagerly
    @Query("SELECT DISTINCT d FROM Document d LEFT JOIN FETCH d.collaborators WHERE d.owner = :user OR :user MEMBER OF d.collaborators")
    List<Document> findAllAccessibleByUser(@Param("user") User user);
    
    List<Document> findByOwner(User owner);
    List<Document> findByCollaboratorsContaining(User user);
    List<Document> findByTitleContainingIgnoreCase(String searchTerm);
}