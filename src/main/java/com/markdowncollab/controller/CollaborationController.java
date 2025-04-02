package com.markdowncollab.controller;

import java.security.Principal;
import com.markdowncollab.dto.CursorPositionMessage;
import com.markdowncollab.dto.DocumentEditMessage;
import com.markdowncollab.model.Document;
import com.markdowncollab.model.User;
import com.markdowncollab.service.CollaborationService;
import com.markdowncollab.service.DocumentService;
import com.markdowncollab.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;

@Controller
public class CollaborationController {

    private final CollaborationService collaborationService;
    private final DocumentService documentService;
    private final UserService userService;

    @Autowired
    public CollaborationController(
            CollaborationService collaborationService,
            DocumentService documentService,
            UserService userService) {
        this.collaborationService = collaborationService;
        this.documentService = documentService;
        this.userService = userService;
    }

    @MessageMapping("/document/{documentId}/edit")
    @SendTo("/topic/document/{documentId}")
    public DocumentEditMessage handleDocumentEdit(
            @DestinationVariable Long documentId,
            DocumentEditMessage message,
            Principal principal) {
        
        // Get the current user
        User user = userService.findByUsername(principal.getName());
        
        // Get the document being edited
        Document document = documentService.findById(documentId);
        
        // Verify user has permission to edit this document
        if (!collaborationService.canUserEdit(user, document)) {
            throw new AccessDeniedException("User does not have permission to edit this document");
        }
        
        // Process the edit
        collaborationService.processEdit(document, user, message);
        
        // Add user information to the message
        message.setUserId(user.getId());
        message.setUsername(user.getUsername());
        
        // Return the message to be broadcast to all subscribers
        return message;
    }
    
    @MessageMapping("/document/{documentId}/cursor")
    @SendTo("/topic/document/{documentId}/cursors")
    public CursorPositionMessage handleCursorUpdate(
            @DestinationVariable Long documentId,
            CursorPositionMessage message,
            Principal principal) {
        
        // Get the current user
        User user = userService.findByUsername(principal.getName());
        
        // Add user information to the message
        message.setUserId(user.getId());
        message.setUsername(user.getUsername());
        
        // Return the message to be broadcast to all subscribers
        return message;
    }
}