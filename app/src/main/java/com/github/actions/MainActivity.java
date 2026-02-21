package com.github.actions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_FILE = 1;
    private EditText etUsername, etToken, etRepo, etFilePath, etContent, etMessage;
    private TextView tvStatus, tvSelectedFile;
    private CheckBox cbSaveCreds;
    private SharedPreferences prefs;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private String selectedFileContent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("GitHubCreds", MODE_PRIVATE);

        etUsername = findViewById(R.id.etUsername);
        etToken = findViewById(R.id.etToken);
        etRepo = findViewById(R.id.etRepo);
        etFilePath = findViewById(R.id.etFilePath);
        etContent = findViewById(R.id.etContent);
        etMessage = findViewById(R.id.etMessage);
        tvStatus = findViewById(R.id.tvStatus);
        tvSelectedFile = findViewById(R.id.tvSelectedFile);
        cbSaveCreds = findViewById(R.id.cbSaveCreds);
        Button btnCommit = findViewById(R.id.btnCommit);
        Button btnSelectFile = findViewById(R.id.btnSelectFile);
        Button btnClearCreds = findViewById(R.id.btnClearCreds);
        Button btnOpenIDE = findViewById(R.id.btnOpenIDE);

        loadSavedCreds();

        btnSelectFile.setOnClickListener(v -> selectFile());
        btnCommit.setOnClickListener(v -> commitAndPush());
        btnClearCreds.setOnClickListener(v -> clearCreds());
        btnOpenIDE.setOnClickListener(v -> {
            startActivity(new Intent(this, EditorActivity.class));
        });
    }

    private void loadSavedCreds() {
        etUsername.setText(prefs.getString("username", ""));
        etToken.setText(prefs.getString("token", ""));
        etRepo.setText(prefs.getString("repo", ""));
        cbSaveCreds.setChecked(prefs.getBoolean("save", false));
    }

    private void saveCreds() {
        if (cbSaveCreds.isChecked()) {
            prefs.edit()
                .putString("username", etUsername.getText().toString())
                .putString("token", etToken.getText().toString())
                .putString("repo", etRepo.getText().toString())
                .putBoolean("save", true)
                .apply();
        }
    }

    private void clearCreds() {
        prefs.edit().clear().apply();
        etUsername.setText("");
        etToken.setText("");
        etRepo.setText("");
        cbSaveCreds.setChecked(false);
        Toast.makeText(this, "Credentials cleared", Toast.LENGTH_SHORT).show();
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                selectedFileContent = sb.toString();
                
                String fileName = null;
                android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        fileName = cursor.getString(nameIndex);
                    }
                    cursor.close();
                }
                
                if (fileName == null) {
                    fileName = uri.getLastPathSegment();
                }
                
                tvSelectedFile.setText("Selected: " + fileName);
                if (etFilePath.getText().toString().isEmpty()) {
                    etFilePath.setText(fileName);
                }
                reader.close();
            } catch (Exception e) {
                tvStatus.setText("Error reading file: " + e.getMessage());
            }
        }
    }

    private void commitAndPush() {
        String username = etUsername.getText().toString();
        String token = etToken.getText().toString();
        String repo = etRepo.getText().toString();
        String filePath = etFilePath.getText().toString();
        String content = etContent.getText().toString();
        String message = etMessage.getText().toString();

        if (username.isEmpty() || token.isEmpty() || repo.isEmpty() || filePath.isEmpty()) {
            tvStatus.setText("Fill all required fields");
            return;
        }

        if (selectedFileContent != null && !selectedFileContent.isEmpty()) {
            content = selectedFileContent;
        }

        if (content.isEmpty()) {
            tvStatus.setText("No content to commit");
            return;
        }

        saveCreds();
        tvStatus.setText("Processing...");
        
        String finalContent = content;
        executor.execute(() -> {
            GitHubAPI api = new GitHubAPI(username, token, repo);
            String result = api.commitAndPush(filePath, finalContent, message);
            runOnUiThread(() -> {
                tvStatus.setText(result);
                if (result.contains("Success")) {
                    etContent.setText("");
                    etMessage.setText("");
                    tvSelectedFile.setText("");
                    selectedFileContent = null;
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
