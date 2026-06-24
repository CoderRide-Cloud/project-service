package com.codingclub.project.service;

import com.codingclub.common.event.ProjectApprovedEvent;
import com.codingclub.common.event.ProjectRejectedEvent;
import com.codingclub.common.event.ProjectSubmittedEvent;
import com.codingclub.common.exception.ResourceNotFoundException;
import com.codingclub.project.model.Project;
import com.codingclub.project.model.Tag;
import com.codingclub.project.repository.ProjectRepository;
import com.codingclub.project.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public List<Project> getApprovedProjects() {
        return projectRepository.findByIsApprovedTrue();
    }

    public List<Project> getProjectsByUserId(Long userId) {
        return projectRepository.findByMemberId(userId);
    }

    public List<Project> getPendingProjects() {
        return projectRepository.findByIsApprovedFalseAndIsRejectedFalseOrderByCreatedAtDesc();
    }

    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + id));
    }

    @Transactional
    public Project createProject(Long memberId, Project project, List<String> tagNames,
                                 String submitterName, String submitterEmail) {
        project.setMemberId(memberId);
        project.setIsApproved(false);
        project.setIsRejected(false);
        project.setTags(resolveTags(tagNames));
        Project saved = projectRepository.save(project);

        kafkaTemplate.send("project-submitted-topic", String.valueOf(saved.getId()),
                new ProjectSubmittedEvent(
                        saved.getId(),
                        saved.getTitle(),
                        saved.getDescription(),
                        saved.getCategory() != null ? saved.getCategory().name() : null,
                        submitterName != null ? submitterName : "Member " + memberId,
                        submitterEmail
                ));

        return saved;
    }

    @Transactional
    public Project updateProject(Long id, Project updatedData, List<String> tagNames,
                                 String submitterName, String submitterEmail) {
        Project existingProject = getProjectById(id);

        if (updatedData.getTitle() != null) existingProject.setTitle(updatedData.getTitle());
        if (updatedData.getDescription() != null) existingProject.setDescription(updatedData.getDescription());
        if (updatedData.getGithubUrl() != null) existingProject.setGithubUrl(updatedData.getGithubUrl());
        if (updatedData.getLiveUrl() != null) existingProject.setLiveUrl(updatedData.getLiveUrl());
        if (updatedData.getImageUrl() != null) existingProject.setImageUrl(updatedData.getImageUrl());
        if (updatedData.getCategory() != null) existingProject.setCategory(updatedData.getCategory());
        if (tagNames != null) {
            existingProject.setTags(resolveTags(tagNames));
        }

        existingProject.setIsApproved(false);
        existingProject.setIsRejected(false);
        existingProject.setRejectionReason(null);

        Project saved = projectRepository.save(existingProject);

        kafkaTemplate.send("project-submitted-topic", String.valueOf(saved.getId()),
                new ProjectSubmittedEvent(
                        saved.getId(),
                        saved.getTitle(),
                        saved.getDescription(),
                        saved.getCategory() != null ? saved.getCategory().name() : null,
                        submitterName != null ? submitterName : "Member " + saved.getMemberId(),
                        submitterEmail
                ));

        return saved;
    }

    @Transactional
    public Project approveProject(Long id, Boolean isApproved, String rejectionReason,
                                  String submitterName, String submitterEmail) {
        Project project = getProjectById(id);
        if (Boolean.TRUE.equals(isApproved)) {
            project.setIsApproved(true);
            project.setIsRejected(false);
            project.setRejectionReason(null);
            Project saved = projectRepository.save(project);
            kafkaTemplate.send("project-approved-topic", String.valueOf(saved.getId()),
                    new ProjectApprovedEvent(
                            saved.getId(),
                            saved.getTitle(),
                            submitterName != null ? submitterName : "Member " + saved.getMemberId(),
                            submitterEmail
                    ));
            return saved;
        }

        project.setIsApproved(false);
        project.setIsRejected(true);
        project.setRejectionReason(rejectionReason);
        Project saved = projectRepository.save(project);
        kafkaTemplate.send("project-rejected-topic", String.valueOf(saved.getId()),
                new ProjectRejectedEvent(
                        saved.getId(),
                        saved.getTitle(),
                        submitterName != null ? submitterName : "Member " + saved.getMemberId(),
                        submitterEmail,
                        rejectionReason
                ));
        return saved;
    }

    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    private Set<Tag> resolveTags(List<String> tagNames) {
        Set<Tag> tags = new HashSet<>();
        if (tagNames == null) {
            return tags;
        }
        for (String rawName : tagNames) {
            if (rawName == null || rawName.isBlank()) {
                continue;
            }
            String name = rawName.startsWith("#") ? rawName.substring(1) : rawName;
            Tag tag = tagRepository.findByName(name).orElseGet(() -> {
                Tag newTag = new Tag();
                newTag.setName(name);
                return tagRepository.save(newTag);
            });
            tags.add(tag);
        }
        return tags;
    }
}
