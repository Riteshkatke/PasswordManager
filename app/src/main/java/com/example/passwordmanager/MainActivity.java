package com.example.passwordmanager;

import android.net.Uri;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText nameInput;
    private EditText websiteInput;
    private EditText usernameInput;
    private EditText passwordInput;
    private EditText passwordLengthInput;
    private PasswordAdapter passwordAdapter;
    private PasswordDatabaseHelper databaseHelper;
    private final List<PasswordModel> passwordList = new ArrayList<>();
    private final ActivityResultLauncher<String> exportDatabaseLauncher =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("application/octet-stream"), uri -> {
                if (uri == null) {
                    return;
                }

                try {
                    exportDatabaseToUri(uri);
                    Toast.makeText(this, R.string.export_success_message, Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Toast.makeText(this, R.string.export_failed_message, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new PasswordDatabaseHelper(this);

        nameInput = findViewById(R.id.nameInput);
        websiteInput = findViewById(R.id.websiteInput);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        passwordLengthInput = findViewById(R.id.passwordLengthInput);
        CheckBox showPasswordCheck = findViewById(R.id.showPasswordCheck);
        Button generateButton = findViewById(R.id.generatePasswordButton);
        Button saveButton = findViewById(R.id.saveButton);
        Button exportButton = findViewById(R.id.exportDatabaseButton);
        RecyclerView recyclerView = findViewById(R.id.passwordRecyclerView);

        passwordAdapter = new PasswordAdapter(this, passwordList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(passwordAdapter);
        recyclerView.setNestedScrollingEnabled(false);

        showPasswordCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                passwordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            passwordInput.setSelection(passwordInput.getText().length());
        });

        generateButton.setOnClickListener(v -> generatePassword());
        saveButton.setOnClickListener(v -> savePassword());
        exportButton.setOnClickListener(v -> startDatabaseExport());

        loadPasswords();
    }

    private void generatePassword() {
        String lengthValue = passwordLengthInput.getText().toString().trim();
        int length = 12;

        if (!TextUtils.isEmpty(lengthValue)) {
            try {
                length = Integer.parseInt(lengthValue);
            } catch (NumberFormatException ignored) {
                Toast.makeText(this, R.string.invalid_length_message, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (length < 4 || length > 32) {
            Toast.makeText(this, R.string.invalid_length_message, Toast.LENGTH_SHORT).show();
            return;
        }

        String generatedPassword = PasswordGenerator.generate(length, true, true, true, true);
        passwordInput.setText(generatedPassword);
        passwordInput.setSelection(generatedPassword.length());
        Toast.makeText(this, R.string.password_generated, Toast.LENGTH_SHORT).show();
    }

    private void savePassword() {
        String name = nameInput.getText().toString().trim();
        String website = websiteInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.validation_message, Toast.LENGTH_SHORT).show();
            return;
        }

        long rowId = databaseHelper.insertPassword(name, website, username, password);
        if (rowId == -1) {
            Toast.makeText(this, R.string.save_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        clearInputs();
        loadPasswords();
        Toast.makeText(this, R.string.save_success, Toast.LENGTH_SHORT).show();
    }

    private void loadPasswords() {
        passwordList.clear();
        passwordList.addAll(databaseHelper.getAllPasswords());
        passwordAdapter.notifyDataSetChanged();
    }

    private void clearInputs() {
        nameInput.setText("");
        websiteInput.setText("");
        usernameInput.setText("");
        passwordInput.setText("");
    }

    private void startDatabaseExport() {
        String fileName = "password_manager_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) +
                ".db";
        exportDatabaseLauncher.launch(fileName);
    }

    private void exportDatabaseToUri(Uri uri) throws IOException {
        databaseHelper.getReadableDatabase().close();
        File databaseFile = getDatabasePath(PasswordDatabaseHelper.DATABASE_NAME);

        if (!databaseFile.exists()) {
            throw new IOException("Database file not found");
        }

        try (InputStream inputStream = new FileInputStream(databaseFile);
             OutputStream outputStream = getContentResolver().openOutputStream(uri, "w")) {
            if (outputStream == null) {
                throw new IOException("Could not open export destination");
            }

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        }
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }
}
