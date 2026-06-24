package com.codingclub.project.seeder;

import com.codingclub.project.model.Tag;
import com.codingclub.project.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final TagRepository tagRepository;

    @Override
    public void run(String... args) {
        log.info("🏷️ Checking tags seeder...");
        
        List<String> tags = List.of(
            "Web Development", "Mobile App", "Desktop App", "Game Development",
            "Machine Learning", "Artificial Intelligence", "Deep Learning",
            "Data Science", "Open Source", "Portfolio", "Hackathon Project",
            "Research Project", "Final Year Project", "Automation", "API Development",
            "UI/UX Design", "Fullstack", "Frontend", "Backend", "Cloud",
            "DevOps", "Cybersecurity", "Competitive Programming", "Embedded Systems",
            "Blockchain", "IoT", "AR/VR", "Software Tool", "Productivity",
            "Utility", "Plugin / Extension", "CLI Tool", "Educational Project",
            "Club Project", "Collaboration"
        );

        int tagsAdded = 0;
        for (String tagName : tags) {
            if (tagRepository.findByName(tagName).isEmpty()) {
                Tag tag = new Tag();
                tag.setName(tagName);
                tagRepository.save(tag);
                tagsAdded++;
            }
        }
        
        log.info("✅ Seeded {} new tags. Total tags checked: {}", tagsAdded, tags.size());
    }
}
