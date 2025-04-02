package com.markdowncollab.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import com.markdowncollab.dto.DocumentEditMessage;
import com.markdowncollab.model.Document;
import com.markdowncollab.model.User;
import com.markdowncollab.pattern.observer.DocumentObserver;
import com.markdowncollab.pattern.observer.DocumentSubject;
import com.markdowncollab.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CollaborationService implements DocumentSubject {

    private final DocumentRepository documentRepository;
    private final List<DocumentObserver> observers = new CopyOnWriteArrayList<>();

    @Autowired
    public CollaborationService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    public void registerObserver(DocumentObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(DocumentObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(DocumentEditMessage change) {
        for (DocumentObserver observer : observers) {
            observer.update(change);
        }
    }

    @Transactional
    public void processEdit(Document document, User user, DocumentEditMessage message) {
        // Update document content based on the edit operation
        if ("insert".equals(message.getOperation())) {
            document.insertText(message.getPosition(), message.getText());
        } else if ("delete".equals(message.getOperation())) {
            document.deleteText(message.getPosition(), message.getLength());
        } else if ("replace".equals(message.getOperation())) {
            document.setContent(message.getText());
        }
        
        // Save the document
        documentRepository.save(document);
        
        // Notify observers about the change
        message.setUserId(user.getId());
        message.setUsername(user.getUsername());
        notifyObservers(message);
    }

    public boolean canUserEdit(User user, Document document) {
        return document.getOwner().getId().equals(user.getId()) ||
                document.getCollaborators().contains(user);
    }
}