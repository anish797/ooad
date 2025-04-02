package com.markdowncollab.pattern.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import com.markdowncollab.model.Document;
import org.springframework.stereotype.Component;

/**
 * Invoker for the Command Pattern. Manages execution of commands and maintains
 * undo/redo history for each document.
 */
@Component
public class DocumentEditor {
    // Map to store command history for each document
    private final Map<Long, Stack<Command>> undoStackMap = new HashMap<>();
    private final Map<Long, Stack<Command>> redoStackMap = new HashMap<>();
    
    public void executeCommand(Document document, Command command) {
        Long documentId = document.getId();
        
        // Initialize stacks if they don't exist for this document
        undoStackMap.computeIfAbsent(documentId, k -> new Stack<>());
        redoStackMap.computeIfAbsent(documentId, k -> new Stack<>());
        
        // Execute the command
        command.execute();
        
        // Add to undo stack
        undoStackMap.get(documentId).push(command);
        
        // Clear redo stack when a new command is executed
        redoStackMap.get(documentId).clear();
    }
    
    public boolean canUndo(Long documentId) {
        return undoStackMap.containsKey(documentId) && !undoStackMap.get(documentId).isEmpty();
    }
    
    public boolean canRedo(Long documentId) {
        return redoStackMap.containsKey(documentId) && !redoStackMap.get(documentId).isEmpty();
    }
    
    public void undo(Long documentId) {
        if (canUndo(documentId)) {
            Command command = undoStackMap.get(documentId).pop();
            command.undo();
            redoStackMap.get(documentId).push(command);
        }
    }
    
    public void redo(Long documentId) {
        if (canRedo(documentId)) {
            Command command = redoStackMap.get(documentId).pop();
            command.execute();
            undoStackMap.get(documentId).push(command);
        }
    }
    
    public void clearHistory(Long documentId) {
        if (undoStackMap.containsKey(documentId)) {
            undoStackMap.get(documentId).clear();
        }
        if (redoStackMap.containsKey(documentId)) {
            redoStackMap.get(documentId).clear();
        }
    }
}