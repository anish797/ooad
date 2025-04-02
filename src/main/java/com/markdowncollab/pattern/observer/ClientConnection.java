package com.markdowncollab.pattern.observer;

import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.markdowncollab.dto.DocumentEditMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * Concrete Observer implementation for WebSocket connections.
 * Represents a client connected to a collaborative editing session.
 */
public class ClientConnection implements DocumentObserver {
    private static final Logger log = LoggerFactory.getLogger(ClientConnection.class);
    
    private final WebSocketSession session;
    private final String userId;
    private final ObjectMapper objectMapper;
    
    public ClientConnection(WebSocketSession session, String userId, ObjectMapper objectMapper) {
        this.session = session;
        this.userId = userId;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void update(DocumentEditMessage change) {
        // Skip sending update to the user who made the change
        if (!userId.equals(change.getUserId().toString())) {
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(change)));
            } catch (IOException e) {
                log.error("Failed to send update to client", e);
            }
        }
    }
    
    public WebSocketSession getSession() {
        return session;
    }
    
    public String getUserId() {
        return userId;
    }
}