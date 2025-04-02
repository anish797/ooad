package com.markdowncollab.repository;

import java.util.List;
import com.markdowncollab.model.Comment;
import com.markdowncollab.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByDocumentOrderByPositionAsc(Document document);
    
    List<Comment> findByDocumentIdOrderByPositionAsc(Long documentId);
}