package com.markdowncollab.pattern.strategy;

/**
 * Strategy interface for implementing different markdown rendering strategies.
 */
public interface MarkdownRenderStrategy {
    /**
     * Render markdown content to HTML
     * 
     * @param markdownContent The markdown content to render
     * @return The rendered HTML
     */
    String render(String markdownContent);
}