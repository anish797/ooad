package com.markdowncollab.pattern.command;

import com.markdowncollab.model.Document;

/**
 * Concrete Command implementation for inserting text into a document.
 */
public class InsertTextCommand implements Command {
    private final Document document;
    private final int position;
    private final String text;
    
    public InsertTextCommand(Document document, int position, String text) {
        this.document = document;
        this.position = position;
        this.text = text;
    }
    
    @Override
    public void execute() {
        document.insertText(position, text);
    }
    
    @Override
    public void undo() {
        document.deleteText(position, text.length());
    }
}