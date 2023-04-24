package ru.iceberg.projects.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.iceberg.projects.entity.User;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

    Optional<User> findById(long id);
}
