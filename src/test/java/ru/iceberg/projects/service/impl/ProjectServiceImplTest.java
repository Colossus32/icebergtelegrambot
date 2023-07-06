package ru.iceberg.projects.service.impl;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import ru.iceberg.projects.entity.Project;
import ru.iceberg.projects.entity.User;
import ru.iceberg.projects.repo.ProjectRepo;
import ru.iceberg.projects.repo.UserRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
class ProjectServiceImplTest {
    @Mock
    private ProjectRepo projectRepo;

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private ProjectServiceImpl projectService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        System.out.println("projectRepo: " + projectRepo);
        System.out.println("userRepo: " + userRepo);
        System.out.println("projectService: " + projectService);
    }

    @Test
    void addWorkerToProject() {
        // Arrange
        long projectId = 1L;
        int workerId = 2;
        Project project = new Project();
        project.setId(projectId);
        project.setName("Project1");
        List<User> userList = new ArrayList<>();
        User user1 = new User();
        user1.setId(1L);
        user1.setName("User1");
        userList.add(user1);
        User user2 = new User();
        user2.setId(2L);
        user2.setName("User2");
        userList.add(user2);


        when(projectRepo.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepo.findAll()).thenReturn(userList);

        // Act
        String result = projectService.addWorkerToProject(projectId, workerId);

        // Assert
        assertEquals(String.format("Проекту %s добавлен работник %s", project.getName(), userList.get(workerId - 1).getName()), result);
        assertTrue(project.getParticipants().contains(String.valueOf(userList.get(workerId - 1).getId())));
    }

    @Test
    void addWorkerToProject_InputError() {
        // Arrange
        long projectId = 1L;
        int workerId = 4;
        Project project = new Project();
        project.setId(projectId);

        List<User> userList = new ArrayList<>();
        User user1 = new User();
        user1.setId(1L);
        user1.setName("User1");
        userList.add(user1);
        User user2 = new User();
        user2.setId(2L);
        user2.setName("User2");
        userList.add(user2);

        when(projectRepo.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepo.findAll()).thenReturn(userList);

        // Act
        String result = projectService.addWorkerToProject(projectId, workerId);

        // Assert
        assertEquals("Ошибка ввода: Недопустимый индекс списка", result);
        assertFalse(project.getParticipants().contains(String.valueOf(userList.get(workerId - 1).getId())));
    }

    @Test
    void addWorkerToProject_InputErrorWhenEmptyList() {
        // Arrange
        long projectId = 1L;
        int workerId = 1;
        Project project = new Project();
        project.setId(projectId);
        List<User> userList = new ArrayList<>();

        when(projectRepo.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepo.findAll()).thenReturn(userList);

        // Act
        String result = projectService.addWorkerToProject(projectId, workerId);

        // Assert
        assertEquals("Ошибка ввода: Список пуст", result);
        assertFalse(project.getParticipants().contains(String.valueOf(userList.get(workerId - 1).getId())));
    }

    @Test
    void addWorkerToProject_ProjectNotFound() {
        // Arrange
        long projectId = 1L;
        int workerId = 2;

        when(projectRepo.findById(projectId)).thenReturn(Optional.empty());

        // Act
        String result = projectService.addWorkerToProject(projectId, workerId);

        // Assert
        assertEquals("Проект не найден", result);
    }
}