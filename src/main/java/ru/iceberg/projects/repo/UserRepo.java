package ru.iceberg.projects.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.iceberg.projects.entity.User;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {

    Optional<User> findById(long id);
}
