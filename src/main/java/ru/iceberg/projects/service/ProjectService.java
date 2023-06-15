package ru.iceberg.projects.service;

import ru.iceberg.projects.entity.Project;

import java.util.Optional;
import java.util.Set;

public interface ProjectService {
    String addProject(Long authorId, String name);
    String addProject(Long authorId, String name, String path);
    String deleteProjectById(long id);
    boolean updateProject(long id, Project project);
    Optional<Project> findById(long id);
    String findProjectsByTag(String tag);
    String addTag (long id,String tag);
    String findProjectsByIsActive();

    String finishProjectById(long id);

    String showAllProjects();

    String changeName(long id, String name);

    String addWorkerToProject(long projectid, int workerid);

    //String reportMail();
    Set<Project> reportMail();

    String findMyActiveProjects(long id);

    String addProjectLong(String body);
}
