// src/main/java/com/example/bdd/InputActivity.java
package com.example.bdd;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InputActivity extends AppCompatActivity {

    private EditText editTextName, editTextZp;
    private Button buttonSave, buttonDelete;
    private PersonDao personDao;
    private int currentPersonId = -1; // -1 означает новый сотрудник

    private final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input); // Убедитесь, что R.layout.activity_input соответствует

        editTextName = findViewById(R.id.editTextName);
        editTextZp = findViewById(R.id.editTextZp); // Изменил id для зарплаты
        buttonSave = findViewById(R.id.buttonSave);
        buttonDelete = findViewById(R.id.buttonDelete);

        personDao = AppDataBase.getDatabase(getApplicationContext()).personDao();

        // Проверяем, передан ли ID сотрудника (для редактирования)
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("person_id")) {
            currentPersonId = extras.getInt("person_id");
            databaseWriteExecutor.execute(() -> {
                Person person = personDao.getPersonById(currentPersonId);
                runOnUiThread(() -> {
                    if (person != null) {
                        editTextName.setText(person.getName());
                        editTextZp.setText(String.valueOf(person.getZp()));
                        buttonDelete.setVisibility(View.VISIBLE); // Показываем кнопку удаления
                    } else {
                        Toast.makeText(InputActivity.this, "Сотрудник не найден", Toast.LENGTH_SHORT).show();
                        finish(); // Закрываем Activity
                    }
                });
            });
        } else {
            buttonDelete.setVisibility(View.GONE); // Скрываем кнопку удаления для нового сотрудника
        }

        buttonSave.setOnClickListener(v -> savePerson());
        buttonDelete.setOnClickListener(v -> deletePerson());
    }

    private void savePerson() {
        String name = editTextName.getText().toString().trim();
        String zpString = editTextZp.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(zpString)) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int zp = Integer.parseInt(zpString);

            if (currentPersonId == -1) {
                Person newPerson = new Person(name, zp);
                databaseWriteExecutor.execute(() -> {
                    personDao.insert(newPerson);
                    runOnUiThread(() -> {
                        Toast.makeText(InputActivity.this, "Сотрудник добавлен", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                });
            } else {
                Person existingPerson = new Person(name, zp);
                existingPerson.setId(currentPersonId);
                databaseWriteExecutor.execute(() -> {
                    personDao.update(existingPerson);
                    runOnUiThread(() -> {
                        Toast.makeText(InputActivity.this, "Сотрудник обновлен", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                });
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Поле ЗП должно быть числом", Toast.LENGTH_SHORT).show();
        }
    }

    private void deletePerson() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Удалить сотрудника")
                .setMessage("Вы уверены, что хотите удалить этого сотрудника?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    if (currentPersonId != -1) {
                        databaseWriteExecutor.execute(() -> {
                            Person personToDelete = personDao.getPersonById(currentPersonId);
                            if (personToDelete != null) {
                                personDao.delete(personToDelete);
                                runOnUiThread(() -> {
                                    Toast.makeText(InputActivity.this, "Сотрудник удален", Toast.LENGTH_SHORT).show();
                                    finish(); // Закрываем Activity
                                });
                            }
                        });
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}