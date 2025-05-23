package com.markdowncollab.ui;

import java.util.List;
import java.util.Optional;
import com.markdowncollab.dto.DocumentDTO;
import com.markdowncollab.dto.UserDTO;
import com.markdowncollab.pattern.command.DocumentEditor;
import com.markdowncollab.service.DocumentService;
import com.markdowncollab.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.web.WebEngine;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import java.io.File;
import javafx.beans.value.ChangeListener;
import java.io.FileOutputStream;
import java.io.IOException;
import com.markdowncollab.model.Version;
import com.markdowncollab.service.VersionService;
import javafx.scene.control.ListCell;

/**
 * Main UI class for the Markdown Editor application.
 */
@Component
public class EditorUI {

    private final DocumentService documentService;
    private final UserService userService;
    private final DocumentEditor documentEditor;
    private final VersionService versionService;

    private Stage primaryStage;
    private TextArea editorTextArea;
    private WebView previewWebView;
    private ListView<String> collaboratorsList;
    private Long currentDocumentId;
    
    @Autowired
    public EditorUI(DocumentService documentService, UserService userService, DocumentEditor documentEditor, VersionService versionService) {
        this.documentService = documentService;
        this.userService = userService;
        this.documentEditor = documentEditor;
        this.versionService = versionService;
    }
    
    public void initialize(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Apply dark theme CSS
        Scene scene = createScene();
        scene.getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());
        
        primaryStage.setScene(scene);
        primaryStage.setTitle("Collaborative Markdown Editor");
        primaryStage.show();
        
        // Check if user is authenticated
        if (userService.isAuthenticated()) {
            loadUserDocuments();
        } else {
            showLoginScreen();
        }
    }
    
    private Scene createScene() {
        // Create the main layout
        BorderPane root = new BorderPane();
        
        // Create top menu bar
        menuBar = new MenuBar();
        updateMenuBar();
        root.setTop(menuBar);
        
        // Create center area with split pane for editor and preview
        SplitPane centerSplitPane = new SplitPane();
        
        // Left side: Editor
        editorTextArea = new TextArea();
        editorTextArea.setWrapText(true);
        editorTextArea.getStyleClass().add("markdown-editor");
        
        // Create text change listener
        textChangeListener = (obs, oldText, newText) -> {
            // Update preview when text changes
            updatePreview(newText);
            
            // Save changes if a document is open
            if (currentDocumentId != null) {
                saveDocumentChanges();
            }
        };
        
        // Add the listener
        editorTextArea.textProperty().addListener(textChangeListener);
        
        VBox editorBox = new VBox(5);
        Label editorLabel = new Label("Markdown Editor");
        editorLabel.getStyleClass().add("section-header");
        editorBox.getChildren().addAll(editorLabel, editorTextArea);
        editorBox.setPadding(new Insets(10));
        
        // Right side: Preview
        previewWebView = new WebView();
        previewWebView.getStyleClass().add("markdown-preview");
        
        // Inject custom CSS for the web view (dark theme for the preview)
        WebEngine engine = previewWebView.getEngine();
        engine.setUserStyleSheetLocation(getClass().getResource("/css/preview-dark.css").toExternalForm());
        
        VBox previewBox = new VBox(5);
        Label previewLabel = new Label("Preview");
        previewLabel.getStyleClass().add("section-header");
        previewBox.getChildren().addAll(previewLabel, previewWebView);
        previewBox.setPadding(new Insets(10));
        
        centerSplitPane.getItems().addAll(editorBox, previewBox);
        centerSplitPane.setDividerPositions(0.5);
        root.setCenter(centerSplitPane);
        
        // Right sidebar for collaborators
        VBox rightSidebar = new VBox(10);
        rightSidebar.setPadding(new Insets(10));
        rightSidebar.setPrefWidth(200);
        rightSidebar.getStyleClass().add("sidebar");
        
        Label collaboratorsLabel = new Label("Collaborators");
        collaboratorsLabel.getStyleClass().add("section-header");
        collaboratorsList = new ListView<>();
        Button inviteButton = new Button("Invite Collaborator");
        inviteButton.setMaxWidth(Double.MAX_VALUE);
        inviteButton.setOnAction(e -> showInviteDialog());
        
        rightSidebar.getChildren().addAll(collaboratorsLabel, collaboratorsList, inviteButton);
        root.setRight(rightSidebar);
        
        // Create the scene with appropriate size
        return new Scene(root, 1200, 800);
    }    

    // Add textChangeListener as a class field
    private ChangeListener<String> textChangeListener;

    private MenuBar menuBar;

    private void updateMenuBar() {
        // Clear existing menus
        menuBar.getMenus().clear();
        
        // File menu
        Menu fileMenu = new Menu("File");
        MenuItem newItem = new MenuItem("New Document");
        newItem.setOnAction(e -> createNewDocument());
        MenuItem openItem = new MenuItem("Open Document");
        openItem.setOnAction(e -> showOpenDocumentDialog());
        MenuItem saveItem = new MenuItem("Save");
        saveItem.setOnAction(e -> saveCurrentDocument());
        
        Menu exportMenu = new Menu("Export As");
        MenuItem exportPdfItem = new MenuItem("PDF");
        exportPdfItem.setOnAction(e -> exportDocument("pdf"));
        MenuItem exportHtmlItem = new MenuItem("HTML");
        exportHtmlItem.setOnAction(e -> exportDocument("html"));
        MenuItem exportDocxItem = new MenuItem("DOCX");
        exportDocxItem.setOnAction(e -> exportDocument("docx"));
        exportMenu.getItems().addAll(exportPdfItem, exportHtmlItem, exportDocxItem);
        
        fileMenu.getItems().addAll(newItem, openItem, saveItem, new SeparatorMenuItem(), exportMenu);
        
        // Edit menu
        Menu editMenu = new Menu("Edit");
        MenuItem undoItem = new MenuItem("Undo");
        undoItem.setOnAction(e -> undo());
        MenuItem redoItem = new MenuItem("Redo");
        redoItem.setOnAction(e -> redo());
        editMenu.getItems().addAll(undoItem, redoItem);
        
        // View menu
        Menu viewMenu = new Menu("View");
        MenuItem versionHistoryItem = new MenuItem("Version History");
        versionHistoryItem.setOnAction(e -> showVersionHistory());
        CheckMenuItem syncScrollItem = new CheckMenuItem("Sync Scrolling");
        syncScrollItem.setSelected(true);
        viewMenu.getItems().addAll(versionHistoryItem, syncScrollItem);
        
        // User menu with current username
        String username = "Not logged in";
        if (userService.isAuthenticated()) {
            UserDTO currentUser = userService.getCurrentUser();
            if (currentUser != null) {
                username = currentUser.getUsername();
            }
        }
        
        Menu userMenu = new Menu("User: " + username);
        MenuItem loginItem = new MenuItem("Login");
        loginItem.setOnAction(e -> showLoginScreen());
        MenuItem logoutItem = new MenuItem("Logout");
        logoutItem.setOnAction(e -> logout());
        
        // Disable the login menu item if already logged in
        loginItem.setDisable(userService.isAuthenticated());
        // Disable the logout menu item if not logged in
        logoutItem.setDisable(!userService.isAuthenticated());
        
        userMenu.getItems().addAll(loginItem, logoutItem);
        
        // Add all menus to the menu bar
        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, userMenu);
        
        // Update menu items based on application state
        saveItem.setDisable(currentDocumentId == null);
        undoItem.setDisable(currentDocumentId == null || !documentEditor.canUndo(currentDocumentId));
        redoItem.setDisable(currentDocumentId == null || !documentEditor.canRedo(currentDocumentId));
        versionHistoryItem.setDisable(currentDocumentId == null);
        exportMenu.setDisable(currentDocumentId == null);
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        
        // File menu
        Menu fileMenu = new Menu("File");
        MenuItem newItem = new MenuItem("New Document");
        newItem.setOnAction(e -> createNewDocument());
        
        MenuItem openItem = new MenuItem("Open Document");
        openItem.setOnAction(e -> showOpenDocumentDialog());
        
        MenuItem saveItem = new MenuItem("Save");
        saveItem.setOnAction(e -> saveCurrentDocument());
        
        Menu exportMenu = new Menu("Export As");
        MenuItem exportPdfItem = new MenuItem("PDF");
        exportPdfItem.setOnAction(e -> exportDocument("pdf"));
        MenuItem exportHtmlItem = new MenuItem("HTML");
        exportHtmlItem.setOnAction(e -> exportDocument("html"));
        MenuItem exportDocxItem = new MenuItem("DOCX");
        exportDocxItem.setOnAction(e -> exportDocument("docx"));
        exportMenu.getItems().addAll(exportPdfItem, exportHtmlItem, exportDocxItem);
        
        fileMenu.getItems().addAll(newItem, openItem, saveItem, new SeparatorMenuItem(), exportMenu);
        
        // Edit menu
        Menu editMenu = new Menu("Edit");
        MenuItem undoItem = new MenuItem("Undo");
        undoItem.setOnAction(e -> undo());
        MenuItem redoItem = new MenuItem("Redo");
        redoItem.setOnAction(e -> redo());
        editMenu.getItems().addAll(undoItem, redoItem);
        
        // View menu
        Menu viewMenu = new Menu("View");
        MenuItem versionHistoryItem = new MenuItem("Version History");
        versionHistoryItem.setOnAction(e -> showVersionHistory());
        CheckMenuItem syncScrollItem = new CheckMenuItem("Sync Scrolling");
        syncScrollItem.setSelected(true);
        viewMenu.getItems().addAll(versionHistoryItem, syncScrollItem);
        
        // User menu
        Menu userMenu = new Menu("User");
        MenuItem loginItem = new MenuItem("Login");
        loginItem.setOnAction(e -> showLoginScreen());
        MenuItem logoutItem = new MenuItem("Logout");
        logoutItem.setOnAction(e -> logout());
        userMenu.getItems().addAll(loginItem, logoutItem);
        
        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, userMenu);
        
        // Update menu item states based on authentication
        updateMenuItemStates();
        
        return menuBar;
    }
    private void updateMenuItemStates() {
        boolean isAuthenticated = userService.isAuthenticated();
        
        // Find menu items
        Menu fileMenu = menuBar.getMenus().get(0);
        MenuItem saveItem = fileMenu.getItems().get(2);
        Menu exportMenu = (Menu) fileMenu.getItems().get(4);
        
        // Disable/enable save and export items based on authentication
        saveItem.setDisable(!isAuthenticated);
        for (MenuItem item : exportMenu.getItems()) {
            item.setDisable(!isAuthenticated);
        }
        
        // You might want to add similar logic for other menus like Edit
        Menu editMenu = menuBar.getMenus().get(1);
        editMenu.getItems().forEach(item -> item.setDisable(!isAuthenticated));
    }
    private void onAuthenticationChanged() {
        updateMenuBar();
        
        if (userService.isAuthenticated()) {
            loadUserDocuments();
        } else {
            // Clear current document and UI
            currentDocumentId = null;
            editorTextArea.clear();
            collaboratorsList.getItems().clear();
            primaryStage.setTitle("Collaborative Markdown Editor");
        }
    }
        
    
    private void updatePreview(String markdownText) {
        if (markdownText == null || markdownText.isEmpty()) {
            previewWebView.getEngine().loadContent("<p>No content to preview</p>");
            return;
        }
        
        // Use the document service to render the markdown
        String htmlContent = documentService.renderMarkdown(markdownText);
        
        // Apply CSS styling
        final String styledHtmlContent = wrapWithStyle(htmlContent);
                
        // Update the preview
        Platform.runLater(() -> {
            previewWebView.getEngine().loadContent(styledHtmlContent);
        });

    }
    
    private String wrapWithStyle(String htmlContent) {
        return "<html><head><style>\n" +
                "body { background-color: #2b2b2b; color: #f0f0f0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; padding: 20px; }\n" +
                "pre { background-color: #363636; padding: 10px; border-radius: 5px; }\n" +
                "code { font-family: monospace; background-color: #363636; padding: 2px 4px; border-radius: 3px; }\n" +
                "h1, h2, h3, h4, h5, h6 { color: #e0e0e0; }\n" +
                "a { color: #6495ED; }\n" +
                "blockquote { border-left: 4px solid #555; padding-left: 10px; color: #aaa; }\n" +
                "table { border-collapse: collapse; width: 100%; }\n" +
                "th, td { border: 1px solid #555; padding: 8px; }\n" +
                "th { background-color: #3c3f41; }\n" +
                "tr:nth-child(even) { background-color: #323232; }\n" +
                "</style></head><body>\n" +
                htmlContent +
                "</body></html>";
    }
    
    private void createNewDocument() {
        TextInputDialog dialog = new TextInputDialog("Untitled Document");
        dialog.setTitle("New Document");
        dialog.setHeaderText("Create a new markdown document");
        dialog.setContentText("Enter document title:");
        
        dialog.showAndWait().ifPresent(title -> {
            try {
                Long documentId = documentService.createDocument(title, "");
                loadDocument(documentId);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", 
                        "Failed to create document", e.getMessage());
            }
        });
    }
    
    private void showOpenDocumentDialog() {
        try {
            // Check for unsaved changes
            if (currentDocumentId != null && hasUnsavedChanges()) {
                Alert saveAlert = new Alert(Alert.AlertType.CONFIRMATION);
                saveAlert.setTitle("Unsaved Changes");
                saveAlert.setHeaderText("Do you want to save changes to the current document?");
                saveAlert.setContentText("Your changes will be lost if you don't save.");
                
                ButtonType saveButton = new ButtonType("Save");
                ButtonType discardButton = new ButtonType("Discard");
                ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                
                saveAlert.getButtonTypes().setAll(saveButton, discardButton, cancelButton);
                
                Optional<ButtonType> result = saveAlert.showAndWait();
                
                if (result.get() == cancelButton) {
                    return; // User cancelled opening new document
                } else if (result.get() == saveButton) {
                    saveCurrentDocument(); // Explicitly save current document
                }
            }
    
            List<DocumentDTO> documents = documentService.getUserDocuments();
            
            if (documents.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "No Documents", 
                        "You don't have any documents", "Create a new document first.");
                return;
            }
            
            // Explicitly log the order of documents
            System.out.println("Documents in order:");
            for (int i = 0; i < documents.size(); i++) {
                System.out.println(i + ": ID " + documents.get(i).getId() + " - " + documents.get(i).getTitle());
            }
            
            ChoiceDialog<DocumentDTO> dialog = new ChoiceDialog<>(documents.get(0), documents);
            dialog.setTitle("Open Document");
            dialog.setHeaderText("Select a document to open");
            dialog.setContentText("Document:");
            
            dialog.showAndWait().ifPresent(document -> {
                System.out.println("Selected document - ID: " + document.getId() + ", Title: " + document.getTitle());
                saveDocumentChanges(); // Save any pending changes before loading new document
                loadDocument(document.getId());
            });
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                    "Failed to load documents", e.getMessage());
        }
    }
    
    
    private boolean hasUnsavedChanges() {
        if (currentDocumentId == null) return false;
        
        try {
            DocumentDTO currentDocument = documentService.getDocumentById(currentDocumentId);
            return !currentDocument.getContent().equals(editorTextArea.getText());
        } catch (Exception e) {
            return false;
        }
    }
    
    private void loadDocument(Long documentId) {
        try {
            // Fetch the document details
            DocumentDTO document = documentService.getDocumentById(documentId);
            
            // Disable text change listener temporarily to prevent recursive saves
            editorTextArea.textProperty().removeListener(textChangeListener);
            
            // Clear any existing content
            editorTextArea.clear();
            collaboratorsList.getItems().clear();
            
            // Update content and UI
            editorTextArea.setText(document.getContent());
            primaryStage.setTitle(document.getTitle() + " - Collaborative Markdown Editor");
            updatePreview(document.getContent());
            
            // Update collaborators list
            if (document.getCollaborators() != null) {
                document.getCollaborators().forEach(user -> 
                    collaboratorsList.getItems().add(user.getDisplayName())
                );
            }
            
            // Set current document ID
            currentDocumentId = documentId;
            
            // Re-enable text change listener AFTER setting content
            editorTextArea.textProperty().addListener(textChangeListener);
            
            // Update menu bar
            updateMenuBar();
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                    "Failed to load document", e.getMessage());
        }
    }
    
    private void saveCurrentDocument() {
        if (currentDocumentId == null) {
            System.out.println("No document to save - returning");
            return;
        }
        
        try {
            // Create a dialog to get version description
            TextInputDialog dialog = new TextInputDialog("Manual save");
            dialog.setTitle("Save Document");
            dialog.setHeaderText("Save document with version");
            dialog.setContentText("Enter version description:");
            
            Optional<String> result = dialog.showAndWait();
            
            if (result.isPresent()) {
                String currentContent = editorTextArea.getText();
                String versionDescription = result.get();
                
                System.out.println("Saving current document with version:");
                System.out.println("Document ID: " + currentDocumentId);
                System.out.println("Version description: " + versionDescription);
                
                // Use the new method that creates a version
                documentService.saveDocumentWithVersion(currentDocumentId, currentContent, versionDescription);
                
                showAlert(Alert.AlertType.INFORMATION, "Document Saved", 
                        "Document saved successfully", "A new version has been created.");
                
                System.out.println("Document saved successfully with version");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                    "Failed to save document", e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Update saveDocumentChanges method
    private void saveDocumentChanges() {
        // Auto-save functionality - now using regular update without version creation
        if (currentDocumentId != null) {
            try {
                String currentContent = editorTextArea.getText();
                
                // Only save if content has actually changed
                DocumentDTO currentDocument = documentService.getDocumentById(currentDocumentId);
                if (!currentContent.equals(currentDocument.getContent())) {
                    documentService.updateDocument(currentDocumentId, currentContent);
                    System.out.println("Auto-saved document ID: " + currentDocumentId + " (no version created)");
                }
            } catch (Exception e) {
                // Silently log error, don't disrupt user with alerts for auto-save
                System.err.println("Auto-save failed: " + e.getMessage());
            }
        }
    }
    
    
    private void exportDocument(String format) {
        if (currentDocumentId == null) {
            showAlert(Alert.AlertType.WARNING, "No Document", 
                    "No document is currently open", "Open or create a document first.");
            return;
        }
        
        try {
            // Export the document
            byte[] content = documentService.exportDocument(currentDocumentId, format);
            
            // Open file chooser dialog
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Document");
            
            // Set default filename and extension
            String defaultFileName = "";
            switch (format.toLowerCase()) {
                case "pdf":
                    fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
                    );
                    defaultFileName = "document.pdf";
                    break;
                case "html":
                    fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("HTML Files", "*.html")
                    );
                    defaultFileName = "document.html";
                    break;
                case "docx":
                    fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Word Documents", "*.docx")
                    );
                    defaultFileName = "document.docx";
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported export format");
            }
            
            fileChooser.setInitialFileName(defaultFileName);
            
            // Show save dialog
            File selectedFile = fileChooser.showSaveDialog(primaryStage);
            
            if (selectedFile != null) {
                // Write content to file
                try (FileOutputStream fos = new FileOutputStream(selectedFile)) {
                    fos.write(content);
                    
                    // Show success message
                    showAlert(Alert.AlertType.INFORMATION, "Export Successful", 
                            "Document exported to " + selectedFile.getAbsolutePath(), null);
                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Export Error", 
                            "Failed to save exported document", e.getMessage());
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Export Error", 
                    "Failed to export document", e.getMessage());
        }
    }
    
    private void showVersionHistory() {
        if (currentDocumentId == null) {
            showAlert(Alert.AlertType.WARNING, "No Document", 
                    "No document is currently open", "Open or create a document first.");
            return;
        }
        
        try {
            // Get versions from service
            List<Version> versions = versionService.getDocumentVersions(currentDocumentId);
            
            if (versions.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "No Versions", 
                        "No versions found", "Save the document to create a version.");
                return;
            }
            
            // Create a dialog to display versions
            Dialog<Version> dialog = new Dialog<>();
            dialog.setTitle("Version History");
            dialog.setHeaderText("Document Version History");
            
            // Create a ListView to show versions
            ListView<Version> versionListView = new ListView<>();
            versionListView.setPrefWidth(400);
            versionListView.setPrefHeight(300);
            
            // Define a custom cell factory using anonymous inner class
            versionListView.setCellFactory(param -> new ListCell<Version>() {
                @Override
                protected void updateItem(Version version, boolean empty) {
                    super.updateItem(version, empty);
                    if (empty || version == null) {
                        setText(null);
                    } else {
                        setText(version.getCreatedAt() + " - " + version.getDescription() + 
                                " (by " + version.getAuthor().getUsername() + ")");
                    }
                }
            });
            
            // Add versions to the list view
            versionListView.getItems().addAll(versions);
            
            // Add buttons
            ButtonType restoreButtonType = new ButtonType("Restore", ButtonBar.ButtonData.OK_DONE);
            ButtonType closeButtonType = ButtonType.CLOSE;
            dialog.getDialogPane().getButtonTypes().addAll(restoreButtonType, closeButtonType);
            
            // Enable restore button only when a version is selected
            Node restoreButton = dialog.getDialogPane().lookupButton(restoreButtonType);
            restoreButton.setDisable(true);
            
            versionListView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
                restoreButton.setDisable(newVal == null);
            });
            
            // Set content
            dialog.getDialogPane().setContent(versionListView);
            
            // Set result converter
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == restoreButtonType) {
                    return versionListView.getSelectionModel().getSelectedItem();
                }
                return null;
            });
            
            // Show dialog and handle result
            Optional<Version> result = dialog.showAndWait();
            result.ifPresent(version -> {
                try {
                    versionService.restoreVersion(version.getId(), userService.getCurrentUser().getId());
                    loadDocument(currentDocumentId);
                    showAlert(Alert.AlertType.INFORMATION, "Version Restored", 
                            "Version has been restored", null);
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", 
                            "Failed to restore version", e.getMessage());
                }
            });
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                    "Failed to load version history", e.getMessage());
        }
    }
    
    private void showInviteDialog() {
        if (currentDocumentId == null) {
            showAlert(Alert.AlertType.WARNING, "No Document", 
                    "No document is currently open", "Open or create a document first.");
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Invite Collaborator");
        dialog.setHeaderText("Invite a user to collaborate");
        dialog.setContentText("Enter username:");
        
        dialog.showAndWait().ifPresent(username -> {
            try {
                boolean success = documentService.addCollaborator(currentDocumentId, username);
                
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", 
                            "User invited successfully", null);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", 
                            "Failed to invite user", "User not found or already a collaborator.");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", 
                        "Failed to invite collaborator", e.getMessage());
            }
        });
    }
    
    private void showLoginScreen() {
        // Create a login dialog
        Dialog<UserDTO> dialog = new Dialog<>();
        dialog.setTitle("Login");
        dialog.setHeaderText("Please enter your credentials");
        
        // Set button types
        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        ButtonType registerButtonType = new ButtonType("Register", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, registerButtonType, ButtonType.CANCEL);
        
        // Create the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        
        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        
        // Enable/Disable login button depending on whether a username was entered
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);
        
        // Validation
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty() || passwordField.getText().trim().isEmpty());
        });
        
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(usernameField.getText().trim().isEmpty() || newValue.trim().isEmpty());
        });
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the username field
        Platform.runLater(() -> usernameField.requestFocus());
        
        // Convert the result to a username-password pair
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                try {
                    boolean success = userService.login(usernameField.getText(), passwordField.getText());
                    if (success) {
                        return userService.getCurrentUser();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Login Failed", 
                                "Invalid username or password", null);
                    }
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Login Error", 
                            "Error during login", e.getMessage());
                }
            } else if (dialogButton == registerButtonType) {
                showRegistrationDialog();
            }
            return null;
        });
        
        Optional<UserDTO> result = dialog.showAndWait();
        result.ifPresent(user -> {
            onAuthenticationChanged();
            updateMenuBar(); // Ensure menu is fully updated
        });
    }
    
    private void showRegistrationDialog() {
        // Create a registration dialog
        Dialog<UserDTO> dialog = new Dialog<>();
        dialog.setTitle("Register");
        dialog.setHeaderText("Create a new account");
        
        // Set button types
        ButtonType registerButtonType = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);
        
        // Create the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField displayNameField = new TextField();
        displayNameField.setPromptText("Display Name");
        
        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Display Name:"), 0, 3);
        grid.add(displayNameField, 1, 3);
        
        // Enable/Disable register button based on form completion
        Node registerButton = dialog.getDialogPane().lookupButton(registerButtonType);
        registerButton.setDisable(true);
        
        // Form validation
        Runnable validateForm = () -> {
            boolean valid = !usernameField.getText().trim().isEmpty() &&
                    !passwordField.getText().trim().isEmpty() &&
                    !emailField.getText().trim().isEmpty();
            registerButton.setDisable(!valid);
        };
        
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
        emailField.textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the username field
        Platform.runLater(() -> usernameField.requestFocus());
        
        // Convert the result to user registration
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == registerButtonType) {
                try {
                    String displayName = displayNameField.getText().trim();
                    if (displayName.isEmpty()) {
                        displayName = usernameField.getText().trim();
                    }
                    
                    UserDTO user = userService.registerUser(
                            usernameField.getText().trim(),
                            passwordField.getText().trim(),
                            emailField.getText().trim(),
                            displayName
                    );
                    
                    // Auto-login after successful registration
                    userService.login(usernameField.getText().trim(), passwordField.getText().trim());
                    
                    return user;
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Registration Error", 
                            "Failed to register user", e.getMessage());
                }
            }
            return null;
        });
        
        Optional<UserDTO> result = dialog.showAndWait();
        
        result.ifPresent(user -> {
            showAlert(Alert.AlertType.INFORMATION, "Registration Successful", 
                    "Welcome, " + user.getDisplayName(), "Your account has been created successfully.");
            loadUserDocuments();
        });
    }
    
    private void logout() {
        userService.logout();
        onAuthenticationChanged();
        showLoginScreen();
    }
        
    private void loadUserDocuments() {
        try {
            List<DocumentDTO> documents = documentService.getUserDocuments();
            
            // Update menu bar to show logged in username
            updateMenuBar();
            
            if (!documents.isEmpty()) {
                // Load the first document and explicitly set currentDocumentId
                Long firstDocumentId = documents.get(0).getId();
                currentDocumentId = firstDocumentId;
                loadDocument(firstDocumentId);
            } else {
                // If no documents, ensure menu items are updated
                currentDocumentId = null;
                updateMenuBar();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                    "Failed to load documents", e.getMessage());
        }
    }
    
    private void undo() {
        if (currentDocumentId != null && documentEditor.canUndo(currentDocumentId)) {
            documentEditor.undo(currentDocumentId);
            // Update UI after undo
            loadDocument(currentDocumentId);
        }
    }
    
    private void redo() {
        if (currentDocumentId != null && documentEditor.canRedo(currentDocumentId)) {
            documentEditor.redo(currentDocumentId);
            // Update UI after redo
            loadDocument(currentDocumentId);
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        // Apply dark theme to the dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());
        
        alert.showAndWait();
    }
}