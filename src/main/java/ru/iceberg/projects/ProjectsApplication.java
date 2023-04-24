package ru.iceberg.projects;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.iceberg.projects.telegrambot.ProjectBot;

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
public class ProjectsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectsApplication.class, args);
        new ProjectBot().listen();
    }

}
