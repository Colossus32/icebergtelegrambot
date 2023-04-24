package ru.iceberg.projects.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.iceberg.projects.entity.Project;

import java.util.Optional;

@Repository
public interface ProjectRepo extends JpaRepository<Project, Long> {
    Optional<Project> findByName(String name);
}
