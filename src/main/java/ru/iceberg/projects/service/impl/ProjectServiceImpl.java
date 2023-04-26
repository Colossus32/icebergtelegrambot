package ru.iceberg.projects.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.iceberg.projects.entity.Project;
import ru.iceberg.projects.entity.User;
import ru.iceberg.projects.repo.ProjectRepo;
import ru.iceberg.projects.repo.UserRepo;
import ru.iceberg.projects.service.ProjectService;
import ru.iceberg.projects.util.IceUtility;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepo projectRepo;
    private final UserRepo userRepo;
    private final String CREATION_ERROR =
            "Не удалось создать проект...\nПричины могут быть:\n-такое название проекта уже используется\n-создатель проекта отсутствует в базе данных";
    private final String INPUT_ERROR = "Ошибка ввода.";

    @Override
    public String addProject(Long authorId, String name) {

        try{
            Optional<User> projectAuthor = userRepo.findById(authorId);
            Optional<Project> project = projectRepo.findByName(name);

            if (projectAuthor.isPresent()){
                if (project.isPresent()) return CREATION_ERROR + 1;
                Project freshProject = new Project(projectAuthor.get(),name);
                projectRepo.save(freshProject);
                return String.format("Проект создан:\nid=%d | %s | %s", freshProject.getId(), freshProject.getName(), freshProject.getPath());
            } else return CREATION_ERROR + 2;
        } catch (Exception e){
            e.printStackTrace();
            return CREATION_ERROR + 3;
        }
    }

    @Override
    public String addProject(Long authorId, String name, String path) {
        if (!new File(path).exists()) return "По этому пути уже существует проект.";
        try{
            Optional<User> projectAuthor = userRepo.findById(authorId);
            Optional<Project> project = projectRepo.findByName(name);

            if (projectAuthor.isPresent()){
                if (project.isPresent()) return CREATION_ERROR;
                Project freshProject = new Project(projectAuthor.get(),name, path);
                projectRepo.save(freshProject);
                return String.format("Проект создан:\nid=%d | %s | %s", freshProject.getId(), freshProject.getName(), freshProject.getPath());
            } else return CREATION_ERROR;
        } catch (Exception e){
            e.printStackTrace();
            return CREATION_ERROR;
        }
    }

    @Override
    public String deleteProjectById(long id) {
        Optional<Project> fromDB = projectRepo.findById(id);
        if (fromDB.isPresent()){
            projectRepo.deleteById(id);
            return String.format("Проект id = %d удален. Но проект остается на сервере по пути: %s", id, fromDB.get().getPath());
        } else return "Проект отсутствует в базе. Удаление не случилось.";

    }

    @Override
    public boolean updateProject(long id, Project donor) {
        Optional<Project> fromDB = projectRepo.findById(id);
        if (fromDB.isPresent()) {
            Project inProcess = fromDB.get();

            inProcess.setDateOfCreation(donor.getDateOfCreation());
            inProcess.setDateOfCompletion(donor.getDateOfCompletion());
            inProcess.setActive(donor.isActive());
            inProcess.setAuthor(donor.getAuthor());
            inProcess.setName(donor.getName());
            inProcess.setPath(donor.getPath());
            inProcess.setParticipants(donor.getParticipants());
            inProcess.setTags(donor.getTags());

            projectRepo.save(inProcess);
            return true;
        }
        return false;
    }

    @Override
    public Optional<Project> findById(long id) {
        return projectRepo.findById(id);
    }

    @Override
    public String findProjectsByTag(String tag) {
        List<Project> result = projectRepo.findAll().stream()
                .filter(element -> findTagMatch(element, tag))
                .sorted((a,b) -> (int) (a.getDateOfCreation().getTime() - b.getDateOfCreation().getTime()))
                .collect(Collectors.toList());
        StringBuilder builder = new StringBuilder();
        for (Project p : result) {
            String active = p.isActive() ? "активен" : "завершен";
            StringBuilder parts = new StringBuilder();
            for (User user : p.getParticipants()) parts.append(user.getName()).append(' ');
            builder.append(String.format("id=%d | %s | %s| %s | %s \n",
                    p.getId(), p.getName(), parts.toString(), active, IceUtility.transformToLongPath(p.getPath())));
        }
        return builder.toString();
    }

    private boolean findTagMatch(Project element, String tag) {
        String longTag = element.getTags();
        if (longTag == null || longTag.equals("")) return false;
        return longTag.contains(tag);
    }

    @Override
    public String addTag(long id, String tag) {
        Optional<Project> fromDB = projectRepo.findById(id);
        if (fromDB.isPresent()) {
            Project toUpdate = fromDB.get();
            tag = tag.replace('-', ' ');
            String existedTags = toUpdate.getTags();
            //if (existedTags.equals("") || existedTags.equals("null")) toUpdate.setTags(tag);
            toUpdate.setTags(toUpdate.getTags() + " " + tag);
            projectRepo.save(toUpdate);
            return String.format("Тэг %s добавлен к проекту %s.", tag, toUpdate.getName());
        } else return "Ошибка добавления тэга к проекту.";
    }

    @Override
    public String findProjectsByIsActive() { //потом переписать это на JDBC template
        List<Project> fromDB = projectRepo.findAll().stream().filter(Project::isActive).collect(Collectors.toList());
        StringBuilder builder = new StringBuilder();
        for (Project project : fromDB) {
            builder.append("id: ").append(project.getId()).append(" | ").append(project.getName()).append(" | ")
                    .append(IceUtility.transformToLongPath(project.getPath())).append('\n');
        }
        return builder.toString().trim();
    }

    @Override
    public String finishProjectById(long id) {
        Optional<Project> fromDB = projectRepo.findById(id);
        if (fromDB.isPresent()){
            Project fresh = fromDB.get();
            if (fresh.getDateOfCompletion() != null) return String.format("Проект %s с id=%d уже завершен.", fresh.getName(), id);
            fresh.setActive(false);
            fresh.setDateOfCompletion(new Date());
            projectRepo.save(fresh);
            return String.format("Проект %s с id=%d успешно завершен %s.", fresh.getName(), id, new Date());
        }
        return "Проект отсутствует в базе.";
    }

    @Override
    public String showAllProjects() {
        List<Project> list = projectRepo.findAll();
        StringBuilder builder = new StringBuilder();
        builder.append("Проекты:\n");
        for (Project project : list) {
            String active = project.isActive() ? "активен" : "завершен";
            builder.append(String.format("id=%d | %s | %s \n", project.getId(), project.getName(), active));
        }
        return builder.toString();
    }

    @Override
    public String changeName(long id, String name) {
        Optional<Project> fromDB = projectRepo.findById(id);
        if (fromDB.isPresent()){
            Project fresh = fromDB.get();
            fresh.setName(name);
            projectRepo.save(fresh);
            return "Успешное изменение название проекта. Теперь это " + fresh.getName();
        }
        return "Ошибка изменения названия проекта.";
    }

    @Override
    public String addWorkerToProject(long projectid, int workerid) {
        Optional<Project> fromDB = projectRepo.findById(projectid);
        if (fromDB.isPresent()){
            Project freshProject = fromDB.get();
            List<User> userList = userRepo.findAll();
            if (workerid > userList.size() || workerid < 1) return INPUT_ERROR;
            else {
                Set<User> oldSet = freshProject.getParticipants();
                User freshUser = userList.get(workerid - 1);
                oldSet.add(freshUser);
                freshProject.setParticipants(oldSet);
                projectRepo.save(freshProject);
                return String.format("Проекту %s добавлен работник %s", freshProject.getName(), freshUser.getName());
            }
        }

        return INPUT_ERROR;
    }

    @Override
    public String reportMail() {
        Set<Project> projectSet = projectRepo.findAll().stream()
                .filter(Project::isActive).collect(Collectors.toSet());

        //id.userName. , projectName_projectName01_...
        Map<String, String> map = new HashMap<>();

        for (Project p : projectSet){
            Set<User> users = p.getParticipants();
            for (User u : users) {
                String codeName = String.format("%d.%s.", u.getId(), u.getName());
                if (!map.containsKey(codeName)) map.put(codeName,"");
                map.put(codeName, map.get(codeName) + p.getName() + "_");
            }
        }

        StringBuilder builder = new StringBuilder();
        for (String key : map.keySet()) {
            builder.append(key).append(map.get(key)).append(',');
        }

        log.info(builder.toString());
        return builder.toString();
    }

    @Override
    public String findMyActiveProjects(long id) {
        List<Project> projectList = projectRepo.findAll();
        StringBuilder builder = new StringBuilder();
        builder.append("Ваши активные проекты:\n");
        for (Project project : projectList) {
            Set<User> users = project.getParticipants();
            for(User u : users) {
                if (u.getId() == id) builder.append(project.getName()).append(' ');
            }
        }
        return builder.toString();
    }
}
