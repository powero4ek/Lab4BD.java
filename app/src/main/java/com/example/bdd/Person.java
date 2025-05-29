// src/main/java/com/example/bdd/Person.java
package com.example.bdd;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "people_table") // Указываем имя таблицы
public class Person {
    @PrimaryKey(autoGenerate = true) // Автоматическая генерация ID
    public int id; // Добавляем id как первичный ключ

    public String name; // Изменяем на 'name' для консистентности
    public int zp;

    // Конструктор для создания нового Person
    public Person(String name, int zp) {
        this.name = name;
        this.zp = zp;
    }

    // Геттеры и Сеттеры
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getZp() {
        return zp;
    }

    public void setZp(int zp) {
        this.zp = zp;
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Name: " + name + ", ZP: " + zp;
    }
}