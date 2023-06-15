package ru.iceberg.projects.service;

import ru.iceberg.projects.entity.User;

import java.util.List;

public interface UserService {
    void saveUser(User user);
    void deleteUserById (long id);
    User findUserById(long id);

    String showAllUsers();

    String getAllIds();
    List<User> getAllIdsJson();
}
