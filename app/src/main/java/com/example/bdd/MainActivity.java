// src/main/java/com/example/bdd/MainActivity.java
package com.example.bdd;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ListView listViewPeople;
    private ArrayAdapter<Person> adapter;
    private List<Person> peopleList;
    private PersonDao personDao;
    private FloatingActionButton fabAddPerson;

    // ExecutorService для выполнения операций с БД в фоновом потоке
    private final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Убедитесь, что R.layout.activity_main соответствует

        listViewPeople = findViewById(R.id.listViewPeople); // Изменено с recyclerViewUsers на listViewPeople
        fabAddPerson = findViewById(R.id.fabAddUser); // fabAddUser остался, если он есть в layout

        personDao = AppDataBase.getDatabase(getApplicationContext()).personDao();

        fabAddPerson.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, InputActivity.class);
            startActivity(intent);
        });

        // Обработка кликов по элементам списка для редактирования
        listViewPeople.setOnItemClickListener((parent, view, position, id) -> {
            Person selectedPerson = peopleList.get(position);
            Intent intent = new Intent(MainActivity.this, InputActivity.class);
            intent.putExtra("person_id", selectedPerson.getId()); // Передаем ID для редактирования
            startActivity(intent);
        });

        // Обработка долгих кликов для удаления
        listViewPeople.setOnItemLongClickListener((parent, view, position, id) -> {
            Person personToDelete = peopleList.get(position);
            new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                    .setTitle("Удалить сотрудника")
                    .setMessage("Вы уверены, что хотите удалить " + personToDelete.getName() + "?")
                    .setPositiveButton("Удалить", (dialog, which) -> {
                        databaseWriteExecutor.execute(() -> {
                            personDao.delete(personToDelete);
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, "Сотрудник " + personToDelete.getName() + " удален", Toast.LENGTH_SHORT).show();
                                loadPeople(); // Обновить список
                            });
                        });
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPeople(); // Загружаем данные каждый раз, когда Activity становится активной
    }

    private void loadPeople() {
        databaseWriteExecutor.execute(() -> {
            peopleList = personDao.getAllPeople();
            runOnUiThread(() -> {
                adapter = new ArrayAdapter<>(MainActivity.this,
                        android.R.layout.simple_list_item_1, peopleList);
                listViewPeople.setAdapter(adapter);
            });
        });
    }
}