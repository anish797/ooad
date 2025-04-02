package com.markdowncollab.model;

import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String username;
    
    private String password;
    
    private String email;
    
    private String displayName;
    
    @OneToMany(mappedBy = "owner")
    private Set<Document> ownedDocuments = new HashSet<>();
    
    @ManyToMany(mappedBy = "collaborators")
    private Set<Document> collaboratingDocuments = new HashSet<>();
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    
    public Set<Document> getOwnedDocuments() { return ownedDocuments; }
    public void setOwnedDocuments(Set<Document> ownedDocuments) { this.ownedDocuments = ownedDocuments; }
    
    public Set<Document> getCollaboratingDocuments() { return collaboratingDocuments; }
    public void setCollaboratingDocuments(Set<Document> collaboratingDocuments) { this.collaboratingDocuments = collaboratingDocuments; }
    
    // Helper methods
    public void addOwnedDocument(Document document) {
        ownedDocuments.add(document);
        document.setOwner(this);
    }
    
    public void removeOwnedDocument(Document document) {
        ownedDocuments.remove(document);
        document.setOwner(null);
    }
    
    public void addCollaboratingDocument(Document document) {
        collaboratingDocuments.add(document);
        document.getCollaborators().add(this);
    }
    
    public void removeCollaboratingDocument(Document document) {
        collaboratingDocuments.remove(document);
        document.getCollaborators().remove(this);
    }
}