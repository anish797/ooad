package com.markdowncollab.pattern.strategy;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Component;

/**
 * Concrete Strategy implementation using the CommonMark library for rendering markdown.
 */
@Component
public class CommonMarkRenderer implements MarkdownRenderStrategy {
    private final Parser parser;
    private final HtmlRenderer renderer;
    
    public CommonMarkRenderer() {
        parser = Parser.builder().build();
        renderer = HtmlRenderer.builder().build();
    }
    
    @Override
    public String render(String markdownContent) {
        Node document = parser.parse(markdownContent);
        return renderer.render(document);
    }
}