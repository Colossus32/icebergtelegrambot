package ru.iceberg.projects.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.iceberg.projects.entity.Project;

public interface ProjectRepo extends JpaRepository<Project, Long> {

}
