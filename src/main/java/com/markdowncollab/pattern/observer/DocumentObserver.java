package com.markdowncollab.pattern.observer;

import com.markdowncollab.dto.DocumentEditMessage;

public interface DocumentObserver {
    void update(DocumentEditMessage change);
}