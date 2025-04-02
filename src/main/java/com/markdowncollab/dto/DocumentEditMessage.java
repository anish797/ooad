package com.markdowncollab.dto;

import java.time.LocalDateTime;

public class DocumentEditMessage {
    private Long userId;
    private String username;
    private String operation; // "insert", "delete", "replace"
    private int position;
    private String text;
    private int length; // For delete operations
    private LocalDateTime timestamp;
    
    public DocumentEditMessage() {
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public int getLength() { return length; }
    public void setLength(int length) { this.length = length; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}