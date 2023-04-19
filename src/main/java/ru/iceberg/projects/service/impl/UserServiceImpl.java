package ru.iceberg.projects.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.iceberg.projects.entity.User;
import ru.iceberg.projects.repo.UserRepo;
import ru.iceberg.projects.service.UserService;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepo repo;

    @Override
    public void saveUser(User user) {
        repo.save(user);
    }

    @Override
    public void deleteUserById(long id) {
        repo.deleteById(id);
    }

    @Override
    public User findUserById(long id) {
        return repo.findById(id).orElse(null);
    }
}
