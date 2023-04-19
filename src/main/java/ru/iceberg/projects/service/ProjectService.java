package ru.iceberg.projects.service;

import ru.iceberg.projects.entity.Project;

import java.util.Set;

public interface ProjectService {
    boolean addProject(Project project);
    void deleteProjectById(long id);
    boolean updateProject(Project project);
    Project findById(long id);
    Set<Project> findProjectsByTag(String tag);
    void addTag (String tag);
    String createPathInDB (String nameForDB);
}
