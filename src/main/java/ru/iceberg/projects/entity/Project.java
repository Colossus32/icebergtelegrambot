package ru.iceberg.projects.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.iceberg.projects.util.IceUtility;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Slf4j
@Entity
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(nullable = false)
    Date dateOfCreation;

    Date dateOfCompletion;

    @Column(nullable = false)
    boolean isActive;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    User author;

    @Column(nullable = false, unique = true)
    String name;

    @Column(nullable = false, unique = true)
    String path;

    String tags;

    @OneToMany(cascade = CascadeType.REMOVE)
    Set<User> participants;

    public Project(User author, String name) {
        this.author = author;
        this.name = name;
        this.dateOfCreation = new Date();
        this.isActive = true;
        this.path = IceUtility.createPath(name);
        this.participants = Set.of(author);
        this.tags = name + " ";
    }

    public Project(User author, String name, String path) {
        this.author = author;
        this.name = name;
        this.path = path;
        this.dateOfCreation = new Date();
        this.isActive = true;
        this.participants = Set.of(author);
        this.tags = name + " ";
    }
}
