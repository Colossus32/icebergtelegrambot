package ru.iceberg.projects.service;

import ru.iceberg.projects.entity.User;

public interface UserService {
    void saveUser(User user);
    void deleteUserById (long id);
    User findUserById(long id);

    String showAllUsers();
}
