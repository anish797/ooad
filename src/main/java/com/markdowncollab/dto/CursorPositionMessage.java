package com.markdowncollab.dto;

public class CursorPositionMessage {
    private Long userId;
    private String username;
    private int position;
    
    // Getters and setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
}