package com.markdowncollab.pattern.strategy;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.springframework.stereotype.Component;

/**
 * Concrete Strategy implementation using the Flexmark library for rendering markdown.
 */
@Component
public class FlexmarkRenderer implements MarkdownRenderStrategy {
    private final Parser parser;
    private final HtmlRenderer renderer;
    
    public FlexmarkRenderer() {
        // Configure options
        MutableDataSet options = new MutableDataSet();
        
        // Create parser and renderer
        parser = Parser.builder(options).build();
        renderer = HtmlRenderer.builder(options).build();
    }
    
    @Override
    public String render(String markdownContent) {
        Node document = parser.parse(markdownContent);
        return renderer.render(document);
    }
}