package ru.iceberg.projects.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.iceberg.projects.entity.User;
import ru.iceberg.projects.service.UserService;

import javax.transaction.Transactional;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserController {

    private final UserService userService;

    @PostMapping
    public void saveUser(@RequestParam("id") long id, @RequestParam("name") String name){

        User checker = userService.findUserById(id);
        if (checker != null) log.error("такой пользователь уже есть в базе.");
        else {
            userService.saveUser(new User(id, name));
            log.info(String.format("Добавлен пользователь %s с id=%d.", name, id));
        }
    }

    @PostMapping("/guests")
    public void saveGuest(@RequestParam("id") long id){
        User checker = userService.findUserById(id);
        if (checker != null) log.error("такой пользователь уже есть в базе.");
        else {
            userService.saveUser(new User(id));
            log.info(String.format("Добавлен пользователь с id=%d.", id));
        }
    }

    @DeleteMapping
    public void deleteUser(@RequestParam("id") long id){
        User checker = userService.findUserById(id);
        if (checker != null) {
            userService.deleteUserById(id);
            log.info(String.format("Пользователь %s с id=%d удален.", checker.getName(),id));
        } else log.error(String.format("Пользователя с id=%d нет в базе.", id));
    }

    @PutMapping
    public void updateUser(@RequestParam(value = "id")long id, @RequestParam(value = "name") String name){
        User fromDB = userService.findUserById(id);
        if (fromDB != null) {
            log.info(String.format("Пользователь с id=%d обновлен.", id));
            userService.saveUser(new User(id, fromDB.getRole(), name));
        }
        else {
            log.warn(String.format("Пользователя с id=%d нет в базе.", id));
            userService.saveUser(new User(id, name));
        }
    }

    @GetMapping("/all")
    public String showAllUsers(){
         return userService.showAllUsers();
    }

    //нужно ли выводить пользователя? пока не понятно
}
