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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepo projectRepo;
    private final UserRepo userRepo;

    @PersistenceContext
    private EntityManager entityManager;

    private final String CREATION_ERROR =
            "Не удалось создать проект...\nПричины могут быть:\n-такое название проекта уже используется\n-создатель проекта отсутствует в базе данных";
    private final String INPUT_ERROR = "Ошибка ввода.";

    @Override
    public String addProject(Long authorId, String name) {

        try {
            Optional<User> projectAuthor = userRepo.findById(authorId);
            Optional<Project> project = projectRepo.findByName(name);

            if (projectAuthor.isPresent()) {
                if (project.isPresent()) return CREATION_ERROR;
                Project freshProject = new Project(projectAuthor.get(), name);
                projectRepo.save(freshProject);
                return String.format("Проект создан:\nid=%d | %s | %s",
                        freshProject.getId(), freshProject.getName(), IceUtility.transformToLongPath(freshProject.getPath()));
            } else return CREATION_ERROR;
        } catch (Exception e) {
            e.printStackTrace();
            return CREATION_ERROR;
        }
    }

    @Override
    public String addProject(Long authorId, String name, String path) {
        if (!new File(path).exists()) return CREATION_ERROR;
        try {
            Optional<User> projectAuthor = userRepo.findById(authorId);
            Optional<Project> project = projectRepo.findByName(name);

            if (projectAuthor.isPresent()) {
                if (project.isPresent()) return CREATION_ERROR;
                Project freshProject = new Project(projectAuthor.get(), name, path);
                projectRepo.save(freshProject);
                return String.format("Проект создан:\nid=%d | %s | %s", freshProject.getId(), freshProject.getName(),
                        IceUtility.transformToLongPath(freshProject.getPath()));
            } else return CREATION_ERROR;
        } catch (Exception e) {
            e.printStackTrace();
            return CREATION_ERROR;
        }
    }

    @Override
    public String deleteProjectById(long id) {
        Optional<Project> fromDB = projectRepo.findById(id);
        if (fromDB.isPresent()) {
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
                .sorted((a, b) -> (int) (a.getDateOfCreation().getTime() - b.getDateOfCreation().getTime()))
                .collect(Collectors.toList());
        StringBuilder builder = new StringBuilder();
        for (Project p : result) {
            String active = p.isActive() ? "активен" : "завершен";
            StringBuilder parts = new StringBuilder();
            //Set<User> participants = p.getParticipants(); //////////////////////////////////////////////////////////" " or "-"
            String[] strParts = p.getParticipants().split(" ");
            Set<User> userSet = new HashSet<>();
            for (String everyPart : strParts) {
                userSet.add(userRepo.getById(Long.parseLong(everyPart)));
            }
            for (User user : userSet) parts.append(user.getName()).append(' ');
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
            //String existedTags = toUpdate.getTags();
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
        if (fromDB.isPresent()) {
            Project fresh = fromDB.get();
            if (fresh.getDateOfCompletion() != null)
                return String.format("Проект %s с id=%d уже завершен.", fresh.getName(), id);
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
            builder.append(String.format("id=%d | %s | %s |\n%s \n---------------------------\n",
                    project.getId(),
                    project.getName(),
                    active,
                    IceUtility.transformToLongPath(project.getPath())));
        }
        return builder.toString();
    }

    @Override
    public String changeName(long id, String name) {
        Optional<Project> fromDB = projectRepo.findById(id);
        if (fromDB.isPresent()) {
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
        if (fromDB.isPresent()) {
            Project freshProject = fromDB.get();
            List<User> userList = userRepo.findAll();
            if (workerid > userList.size() || workerid < 1) return INPUT_ERROR + " list";
            else {
                User freshUser = userList.get(workerid - 1);

                if (!freshProject.getParticipants().contains(String.valueOf(freshUser.getId()))) {
                    freshProject.setParticipants(freshProject.getParticipants() + freshUser.getId() + " ");
                }
                projectRepo.save(freshProject);
                return String.format("Проекту %s добавлен работник %s", freshProject.getName(), freshUser.getName());
            }
        }

        return INPUT_ERROR;
    }

    @Override
    public Set<Project> reportMail() {

        String sqlQuery = "SELECT p FROM Project p WHERE p.isActive = true";

        TypedQuery<Project> query = entityManager.createQuery(sqlQuery, Project.class);
        List<Project> resultList = query.getResultList();
        return new HashSet<>(resultList);
    }

    @Override
    public String findMyActiveProjects(long id) {

        Set<Project> projectSet = reportMail();
        StringBuilder builder = new StringBuilder();
        builder.append("Ваши активные проекты:\n");

        for (Project project : projectSet) {
            if (project.getParticipants().contains(String.valueOf(id))) {
                builder.append(String.format("- %s   %s\n", project.getName(), IceUtility.transformToLongPath(project.getPath())));
            }
        }
        return builder.toString();
    }

    @Override
    public String addProjectLong(String body) {

        long id = Long.parseLong(body.substring(0, body.indexOf(' ')));
        body = body.substring(body.indexOf(' ') + 1);

        String name = body.substring(0, body.indexOf('\\') - 1).trim();
        body = body.substring(body.indexOf('\\'));

        String path = body;
        System.out.println("Check path : " + path);

        Optional<Project> fromDB = projectRepo.findByName(name);
        if (fromDB.isPresent()) {
            log.error("Ошибка создания проекта. Проект {} уже существует.", name);
            return "Ошибка создания существующего проекта. Проект уже существует.";
        }

        //проверка существования пути
        path = IceUtility.transformToZ(path);
        File check = new File(path);
        if (!check.exists()) {
            log.error("Ошибка создания проекта. Путь {} не найден.", path);
            return "Ошибка создания существующего проекта. Путь не найден.";
        }

        Optional<User> userFromDB = userRepo.findById(id);
        System.out.println("Present : " + userFromDB.isPresent());
        if (userFromDB.isPresent()) {
            projectRepo.save(new Project(userFromDB.get(), name, path));
            log.info("Проект {} добавлен", name);
            return String.format("Существующий проект по пути %s добавлен с названием %s", IceUtility.transformToLongPath(path), name);
        } else {
            log.error("Ошибка создания проекта. Пользователь {} не найден.", id);
            return "Ошибка создания существующего проекта. Пользователь не найден.";
        }
    }

    @Override
    public String wakeUpProjectById(long id) {
        Optional<Project> fromDB = projectRepo.findById(id);
        if (fromDB.isPresent()) {
            Project existed = fromDB.get();
            existed.setDateOfCompletion(null);
            existed.setActive(true);
            projectRepo.save(existed);
            log.info("У проекта с id={} изменена активность и удалена дата завершения.", id);
            return String.format("Проект с id=%d изменен. Теперь он активен и убрана дата завершения.", id);
        } else {
            log.warn("Ошибка перевода проекта id={} в активное состояние.", id);
            return String.format("Проекта с id=%d нет в базе.", id);
        }
    }

    @Override
    public String addSubProject(String sub) {
        String[] strings = sub.split("/");
        strings[0] = strings[0].replace(' ', '-');
        Optional<Project> fromDB = projectRepo.findByName(strings[0]);
        if (fromDB.isPresent()) {
            Project generalProject = fromDB.get();
            String oldPath = generalProject.getPath();
            String newPath = IceUtility.createNewPathFromOld(oldPath, strings[1]);
            log.info("{} - путь подпроета", newPath);
            Project fresh = new Project(generalProject.getAuthor(), strings[1], newPath);
            projectRepo.save(fresh);
            IceUtility.createDirectoryTree(fresh.getPath());
            log.info("{} - подпроект создан в директории {}", fresh.getName(), IceUtility.transformToLongPath(fresh.getPath()));
            return String.format("В %s создан подпроект %s по пути %s", generalProject.getName(), fresh.getName(), fresh.getPath());
        } else return "Нет проекта " + strings[0] + " в базе. Невозможно создать подпроект.";
    }
}
