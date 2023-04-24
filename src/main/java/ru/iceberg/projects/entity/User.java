package ru.iceberg.projects.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class User {

    @Id //as telegram chatId
    long id;

    @Column(nullable = false)
    int role;

    @Column(nullable = false) // может и null держать ?
    String name;

    public User(long id, String name) {
        this.id = id;
        this.role = 1;
        this.name = name;
    }

    public User(long id) {
        this.id = id;
        this.role = 1;
        this.name = "guest";
    }
}
