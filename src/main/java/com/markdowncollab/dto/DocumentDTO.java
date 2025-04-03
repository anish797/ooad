package com.markdowncollab.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import com.markdowncollab.model.Document;

public class DocumentDTO {
    private Long id;
    private String title;
    private String content;
    private UserDTO owner;
    private List<UserDTO> collaborators;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public DocumentDTO() {
        this.collaborators = new ArrayList<>();
    }
    
    public DocumentDTO(Document document) {
        this.id = document.getId();
        this.title = document.getTitle();
        this.content = document.getContent();
        this.createdAt = document.getCreatedAt();
        this.updatedAt = document.getUpdatedAt();
        
        if (document.getOwner() != null) {
            this.owner = new UserDTO(document.getOwner());
        }
        
        this.collaborators = new ArrayList<>();
        if (document.getCollaborators() != null) {
            this.collaborators = document.getCollaborators().stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
        }
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public UserDTO getOwner() { return owner; }
    public void setOwner(UserDTO owner) { this.owner = owner; }
    
    public List<UserDTO> getCollaborators() { return collaborators; }
    public void setCollaborators(List<UserDTO> collaborators) { this.collaborators = collaborators; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return this.title != null ? this.title : "Untitled Document";
    }
}