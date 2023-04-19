package ru.iceberg.projects.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    long chatId;

    @Column(nullable = false)
    int role;

    @Column(nullable = false) // может и null держать ?
    String name;

    public User(long chatId, String name) {
        this.chatId = chatId;
        this.role = 1;
        this.name = name;
    }

}
