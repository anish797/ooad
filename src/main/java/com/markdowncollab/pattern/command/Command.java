package com.markdowncollab.pattern.command;

/**
 * Command interface for implementing the Command Pattern.
 * This is used for operations like inserting or deleting text in a document,
 * allowing for undo/redo functionality.
 */
public interface Command {
    /**
     * Execute the command
     */
    void execute();
    
    /**
     * Undo the command
     */
    void undo();
}