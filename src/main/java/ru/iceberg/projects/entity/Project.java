package ru.iceberg.projects.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.Set;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Project {
    long id;
    Date dateOfCreation;
    Date dateOfCompletion;
    boolean isActive;
    User author;
    String name;
    String path;
    Set<String> tags;
    Set<User> participants;

}
