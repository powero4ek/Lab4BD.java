// src/main/java/com/example/bdd/PersonDao.java
package com.example.bdd;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update; // Добавлено для обновления

import java.util.List;

@Dao
public interface PersonDao {

    // Добавление Person в бд
    @Insert
    void insert(Person person); // Изменено с insertAll, чтобы вставлять по одному

    // Обновление Person в бд
    @Update // Добавлено
    void update(Person person);

    // Удаление Person из бд
    @Delete
    void delete(Person person);

    // Получение всех Person из бд
    @Query("SELECT * FROM people_table ORDER BY name ASC") // Изменено имя таблицы
    List<Person> getAllPeople();

    // Получение Person по ID (для редактирования)
    @Query("SELECT * FROM people_table WHERE id = :personId LIMIT 1")
    Person getPersonById(int personId);
}