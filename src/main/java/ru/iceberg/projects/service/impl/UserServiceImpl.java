package ru.iceberg.projects.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.iceberg.projects.entity.User;
import ru.iceberg.projects.repo.UserRepo;
import ru.iceberg.projects.service.UserService;

import java.util.List;
import java.util.Set;

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

    @Override
    public String showAllUsers() {
        StringBuilder builder = new StringBuilder();
        builder.append("Работники:\n");

        List<User> list = repo.findAll();
        int index = 1;
        for (User user : list){
            builder.append(String.format("№=%d | %s\n", index++, user.getName()));
        }
        return builder.toString();
    }

    @Override
    public String getAllIds() {
        StringBuilder builder = new StringBuilder();
        List<User> userList = repo.findAll();
        for (User u : userList) builder.append(u.getId()).append(' ');
        return builder.toString();
    }

    @Override
    public List<User> getAllIdsJson() {
        return repo.findAll();
    }
}
