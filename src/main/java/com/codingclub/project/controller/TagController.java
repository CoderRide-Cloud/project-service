package com.codingclub.project.controller;

import com.codingclub.common.security.AuthUserContext;
import com.codingclub.common.security.AuthorizationService;
import com.codingclub.common.security.Permission;
import com.codingclub.common.web.AuthContextResolver;
import com.codingclub.project.model.Tag;
import com.codingclub.project.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tags")
public class TagController {

    @Autowired
    private TagService tagService;

    @Autowired
    private AuthContextResolver authContextResolver;

    @Autowired
    private AuthorizationService authorizationService;

    @GetMapping("/search")
    public ResponseEntity<List<Tag>> searchTags(@RequestParam(required = false) String q) {
        return ResponseEntity.ok(tagService.searchTags(q));
    }

    @GetMapping
    public ResponseEntity<List<Tag>> getPaginatedTags(
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(tagService.getAllTags(skip, limit));
    }

    @PostMapping
    public ResponseEntity<Tag> createTag(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Permissions", required = false) String permissions,
            @RequestHeader(value = "X-User-Position", required = false) String position,
            @RequestHeader(value = "X-User-Is-Lead", required = false) String isLead,
            @RequestHeader(value = "X-User-Is-Active", required = false) String isActive,
            @RequestHeader(value = "X-User-Custom-Role-Id", required = false) String customRoleId,
            @RequestBody Map<String, String> payload) {

        AuthUserContext authUser = authContextResolver.resolve(userId, role, permissions, position, isLead, isActive, customRoleId);
        authorizationService.requirePermission(authUser, Permission.MANAGE_TAGS);

        String name = payload.get("name");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tag name is required");
        }

        return ResponseEntity.status(201).body(tagService.createTag(name));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteTag(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Permissions", required = false) String permissions,
            @RequestHeader(value = "X-User-Position", required = false) String position,
            @RequestHeader(value = "X-User-Is-Lead", required = false) String isLead,
            @RequestHeader(value = "X-User-Is-Active", required = false) String isActive,
            @RequestHeader(value = "X-User-Custom-Role-Id", required = false) String customRoleId,
            @PathVariable Long id) {

        AuthUserContext authUser = authContextResolver.resolve(userId, role, permissions, position, isLead, isActive, customRoleId);
        authorizationService.requirePermission(authUser, Permission.MANAGE_TAGS);

        tagService.deleteTag(id);
        return ResponseEntity.ok(Map.of("message", "Tag deleted successfully"));
    }
}
