package com.markdowncollab.service;

import com.markdowncollab.pattern.strategy.MarkdownRenderStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MarkdownService {
    
    private MarkdownRenderStrategy renderStrategy;
    
    @Autowired
    public MarkdownService(MarkdownRenderStrategy defaultRenderStrategy) {
        this.renderStrategy = defaultRenderStrategy;
    }
    
    public void setRenderStrategy(MarkdownRenderStrategy renderStrategy) {
        this.renderStrategy = renderStrategy;
    }
    
    public String renderMarkdown(String markdownContent) {
        return renderStrategy.render(markdownContent);
    }
}