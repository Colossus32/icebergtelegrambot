package ru.iceberg.projects.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class User {
    long id;
    Role role;
    String name;
    long chatId;
}
