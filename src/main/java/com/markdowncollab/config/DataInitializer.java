package com.markdowncollab.config;

import com.markdowncollab.model.Document;
import com.markdowncollab.model.User;
import com.markdowncollab.repository.DocumentRepository;
import com.markdowncollab.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initializes the database with some test data when the application starts.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(UserRepository userRepository, DocumentRepository documentRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create test users if they don't exist
        createTestUsers();
        
        // Create test documents
        createTestDocuments();
    }
    
    private void createTestUsers() {
        if (userRepository.count() == 0) {
            // Create admin user
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setEmail("admin@example.com");
            admin.setDisplayName("Administrator");
            userRepository.save(admin);
            
            // Create test user
            User testUser = new User();
            testUser.setUsername("user");
            testUser.setPassword(passwordEncoder.encode("user"));
            testUser.setEmail("user@example.com");
            testUser.setDisplayName("Test User");
            userRepository.save(testUser);
            
            System.out.println("Test users created.");
        }
    }
    
    private void createTestDocuments() {
        if (documentRepository.count() == 0) {
            // Find admin user
            User admin = userRepository.findByUsername("admin").orElseThrow();
            
            // Create sample document
            Document document = new Document();
            document.setTitle("Getting Started with Markdown");
            document.setContent(getSampleMarkdown());
            document.setOwner(admin);
            documentRepository.save(document);
            
            // Create another sample document
            Document document2 = new Document();
            document2.setTitle("Project Ideas");
            document2.setContent("# Project Ideas\n\n## Web Applications\n\n- To-do list app\n- Blog platform\n- E-commerce site\n\n## Mobile Apps\n\n- Weather app\n- Fitness tracker\n- Recipe manager");
            document2.setOwner(admin);
            documentRepository.save(document2);
            
            System.out.println("Test documents created.");
        }
    }
    
    private String getSampleMarkdown() {
        return "# Markdown Guide\n\n" +
                "## Introduction\n\n" +
                "Markdown is a lightweight markup language that you can use to add formatting elements to plaintext text documents.\n\n" +
                "## Basic Syntax\n\n" +
                "### Headings\n\n" +
                "# Heading 1\n" +
                "## Heading 2\n" +
                "### Heading 3\n\n" +
                "### Bold and Italic\n\n" +
                "**Bold text**\n" +
                "*Italic text*\n" +
                "***Bold and italic text***\n\n" +
                "### Lists\n\n" +
                "Unordered list:\n" +
                "- Item 1\n" +
                "- Item 2\n" +
                "- Item 3\n\n" +
                "Ordered list:\n" +
                "1. First item\n" +
                "2. Second item\n" +
                "3. Third item\n\n" +
                "### Links\n\n" +
                "[Visit GitHub](https://github.com)\n\n" +
                "### Images\n\n" +
                "![Alt text](https://via.placeholder.com/150)\n\n" +
                "### Code\n\n" +
                "Inline code: `var example = true`\n\n" +
                "Code block:\n" +
                "```java\n" +
                "public class HelloWorld {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Hello, World!\");\n" +
                "    }\n" +
                "}\n" +
                "```\n\n" +
                "### Blockquotes\n\n" +
                "> This is a blockquote\n" +
                "> It can span multiple lines\n\n" +
                "### Horizontal Rule\n\n" +
                "---\n\n" +
                "## Advanced Features\n\n" +
                "### Tables\n\n" +
                "| Header 1 | Header 2 | Header 3 |\n" +
                "|----------|----------|----------|\n" +
                "| Cell 1   | Cell 2   | Cell 3   |\n" +
                "| Cell 4   | Cell 5   | Cell 6   |\n\n" +
                "### Task Lists\n\n" +
                "- [x] Task 1\n" +
                "- [ ] Task 2\n" +
                "- [ ] Task 3\n\n" +
                "## Conclusion\n\n" +
                "Markdown is easy to learn and use. This collaborative editor makes it even easier to work with teammates on documentation, notes, and other text-based content.";
    }
}