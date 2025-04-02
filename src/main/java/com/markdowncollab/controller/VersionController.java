package com.markdowncollab.controller;

import java.util.List;
import com.markdowncollab.model.Version;
import com.markdowncollab.service.UserService;
import com.markdowncollab.service.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/versions")
public class VersionController {

    private final VersionService versionService;
    private final UserService userService;

    @Autowired
    public VersionController(VersionService versionService, UserService userService) {
        this.versionService = versionService;
        this.userService = userService;
    }

    @GetMapping("/document/{documentId}")
    public ResponseEntity<List<Version>> getDocumentVersions(@PathVariable Long documentId) {
        List<Version> versions = versionService.getDocumentVersions(documentId);
        return ResponseEntity.ok(versions);
    }

    @GetMapping("/{versionId}")
    public ResponseEntity<Version> getVersion(@PathVariable Long versionId) {
        return versionService.getVersion(versionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/document/{documentId}")
    public ResponseEntity<Version> createVersion(
            @PathVariable Long documentId,
            @RequestParam String content,
            @RequestParam String description) {
        
        Long userId = userService.getCurrentUser().getId();
        Version version = versionService.createVersion(documentId, content, description, userId);
        return ResponseEntity.ok(version);
    }

    @PostMapping("/{versionId}/restore")
    public ResponseEntity<?> restoreVersion(@PathVariable Long versionId) {
        Long userId = userService.getCurrentUser().getId();
        versionService.restoreVersion(versionId, userId);
        return ResponseEntity.ok().build();
    }
}