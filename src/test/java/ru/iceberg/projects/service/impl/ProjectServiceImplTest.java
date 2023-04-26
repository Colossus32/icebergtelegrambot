package ru.iceberg.projects.service.impl;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.Query;
import org.springframework.test.context.TestPropertySource;
import ru.iceberg.projects.entity.User;
import ru.iceberg.projects.repo.ProjectRepo;
import ru.iceberg.projects.repo.UserRepo;
import ru.iceberg.projects.service.ProjectService;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestPropertySource("/applicationtest.properties")
class ProjectServiceImplTest {

    @Autowired
    private ProjectRepo projectRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private ProjectService projectService;


    @BeforeEach
    void setUp() {
        userRepo.save(new User(111L, "kirill"));
    }

    @AfterEach
    void tearDown() {
        projectRepo.deleteById(1L);
        userRepo.deleteById(111L);
    }

    @Test
    void should_add_one_project() {
        String response = projectService.addProject(111L,"тест");
        String answer = String.format("Проект создан:\nid=%d | %s | %s", 1, "тест",  "Z:/!Л-Я/Т/тест");
        assertEquals(answer, response);
    }

    @Test
    void testAddProject() {
    }

    @Test
    void deleteProjectById() {
    }

    @Test
    void updateProject() {
    }

    @Test
    void findById() {
    }

    @Test
    void findProjectsByTag() {
    }

    @Test
    void addTag() {
    }

    @Test
    void findProjectsByIsActive() {
    }

    @Test
    void finishProjectById() {
    }

    @Test
    void showAllProjects() {
    }

    @Test
    void changeName() {
    }

    @Test
    void addWorkerToProject() {
    }

    @Test
    void reportMail() {
    }

    @Test
    void findMyActiveProjects() {
    }
}