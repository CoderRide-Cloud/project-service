package com.codingclub.project.dto;

import com.codingclub.project.model.Project;
import com.codingclub.project.model.ProjectCategory;
import lombok.Data;

import java.util.List;

@Data
public class ProjectRequest {
    private String title;
    private String description;
    private String githubUrl;
    private String liveUrl;
    private String imageUrl;
    private ProjectCategory category;
    private List<String> tags;
    private String submitterName;
    private String submitterEmail;

    public Project toProject() {
        Project project = new Project();
        project.setTitle(title);
        project.setDescription(description);
        project.setGithubUrl(githubUrl);
        project.setLiveUrl(liveUrl);
        project.setImageUrl(imageUrl);
        project.setCategory(category);
        return project;
    }
}
