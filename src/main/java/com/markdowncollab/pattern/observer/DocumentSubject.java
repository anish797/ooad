package com.markdowncollab.pattern.observer;

import com.markdowncollab.dto.DocumentEditMessage;

public interface DocumentSubject {
    void registerObserver(DocumentObserver observer);
    void removeObserver(DocumentObserver observer);
    void notifyObservers(DocumentEditMessage change);
}