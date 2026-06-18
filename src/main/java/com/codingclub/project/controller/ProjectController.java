package com.codingclub.project.controller;

import com.codingclub.common.exception.UnauthorizedException;
import com.codingclub.project.model.Project;
import com.codingclub.project.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects(@RequestParam(required = false) Boolean approvedOnly) {
        if (Boolean.TRUE.equals(approvedOnly)) {
            return ResponseEntity.ok(projectService.getApprovedProjects());
        }
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PostMapping
    public ResponseEntity<Project> createProject(
            @RequestHeader("X-User-Id") String userIdHeader,
            @RequestBody Project project) {
        Long memberId = Long.valueOf(userIdHeader); // For simplicity, assuming user ID equals member ID or member-service maps it
        return ResponseEntity.ok(projectService.createProject(memberId, project));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(
            @RequestHeader("X-User-Id") String userIdHeader,
            @RequestHeader("X-User-Role") String userRole,
            @PathVariable Long id,
            @RequestBody Project project) {
        
        Project existing = projectService.getProjectById(id);
        Long userId = Long.valueOf(userIdHeader);

        if (!existing.getMemberId().equals(userId) && !"ADMIN".equals(userRole)) {
            throw new UnauthorizedException("Not authorized to update this project");
        }

        return ResponseEntity.ok(projectService.updateProject(id, project));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @RequestHeader("X-User-Id") String userIdHeader,
            @RequestHeader("X-User-Role") String userRole,
            @PathVariable Long id) {
        
        Project existing = projectService.getProjectById(id);
        Long userId = Long.valueOf(userIdHeader);

        if (!existing.getMemberId().equals(userId) && !"ADMIN".equals(userRole)) {
            throw new UnauthorizedException("Not authorized to delete this project");
        }

        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    // Admin endpoints integrated here instead of a monolithic admin-service
    @PutMapping("/{id}/approval")
    public ResponseEntity<Project> approveProject(
            @RequestHeader("X-User-Role") String userRole,
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        
        if (!"ADMIN".equals(userRole)) {
            throw new UnauthorizedException("Admin access required");
        }

        Boolean isApproved = (Boolean) payload.get("isApproved");
        String reason = (String) payload.get("reason");

        return ResponseEntity.ok(projectService.approveProject(id, isApproved, reason));
    }
}
