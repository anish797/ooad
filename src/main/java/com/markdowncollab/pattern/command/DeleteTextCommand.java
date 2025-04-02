package com.markdowncollab.pattern.command;

import com.markdowncollab.model.Document;

/**
 * Concrete Command implementation for deleting text from a document.
 */
public class DeleteTextCommand implements Command {
    private final Document document;
    private final int position;
    private final int length;
    private String deletedText; // Stored for undo operation
    
    public DeleteTextCommand(Document document, int position, int length) {
        this.document = document;
        this.position = position;
        this.length = length;
    }
    
    @Override
    public void execute() {
        // Save the text that will be deleted for undo operation
        deletedText = document.getTextRange(position, length);
        document.deleteText(position, length);
    }
    
    @Override
    public void undo() {
        document.insertText(position, deletedText);
    }
}