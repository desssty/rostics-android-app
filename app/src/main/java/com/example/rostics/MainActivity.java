package com.example.rostics;


import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.TextView;
import com.example.rostics.database.DatabaseHelper;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textViewData);

        dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();


        Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'android_metadata' AND name NOT LIKE 'sqlite_%' ORDER BY name;",
                null
        );

        StringBuilder sb = new StringBuilder();
        if (cursor.moveToFirst()) {
            do {
                String tableName = cursor.getString(0);
                sb.append("Таблица: ").append(tableName).append("\n");

                // Для каждой таблицы — вытащим первые строки
                Cursor tableData = db.rawQuery("SELECT * FROM " + tableName + " LIMIT 3", null);
                String[] columns = tableData.getColumnNames();

                sb.append("  Колонки: ").append(String.join(", ", columns)).append("\n");

                if (tableData.moveToFirst()) {
                    do {
                        sb.append("  ▸ ");
                        for (int i = 0; i < columns.length; i++) {
                            sb.append(columns[i]).append("=")
                                    .append(tableData.getString(i))
                                    .append("; ");
                        }
                        sb.append("\n");
                    } while (tableData.moveToNext());
                } else {
                    sb.append("  (пусто)\n");
                }

                sb.append("\n");
                tableData.close();
            } while (cursor.moveToNext());
        } else {
            sb.append("База данных пуста — нет таблиц");
        }

        cursor.close();
        textView.setText(sb.toString());

    }
}