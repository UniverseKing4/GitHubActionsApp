package com.github.actions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_FILE = 1;
    private TextInputEditText etUsername, etToken, etRepo, etFilePath, etContent, etMessage;
    private MaterialTextView tvStatus, tvSelectedFile;
    private MaterialCheckBox cbSaveCreds;
    private CardView statusCard;
    private SharedPreferences prefs;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private String selectedFileContent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("GitHubCreds", MODE_PRIVATE);

        initViews();
        loadSavedCreds();
        setupListeners();
        animateContent();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etToken = findViewById(R.id.etToken);
        etRepo = findViewById(R.id.etRepo);
        etFilePath = findViewById(R.id.etFilePath);
        etContent = findViewById(R.id.etContent);
        etMessage = findViewById(R.id.etMessage);
        tvStatus = findViewById(R.id.tvStatus);
        tvSelectedFile = findViewById(R.id.tvSelectedFile);
        cbSaveCreds = findViewById(R.id.cbSaveCreds);
        statusCard = (CardView) tvStatus.getParent();
        
        MaterialButton btnCommit = findViewById(R.id.btnCommit);
        MaterialButton btnSelectFile = findViewById(R.id.btnSelectFile);
        MaterialButton btnClearCreds = findViewById(R.id.btnClearCreds);
        MaterialButton btnOpenIDE = findViewById(R.id.btnOpenIDE);
    }

    private void setupListeners() {
        findViewById(R.id.btnSelectFile).setOnClickListener(v -> selectFile());
        findViewById(R.id.btnCommit).setOnClickListener(v -> commitAndPush());
        findViewById(R.id.btnClearCreds).setOnClickListener(v -> clearCreds());
        findViewById(R.id.btnOpenIDE).setOnClickListener(v -> {
            startActivity(new Intent(this, EditorActivity.class));
            overridePendingTransition(R.anim.fade_in_up, R.anim.fade_out);
        });
    }

    private void animateContent() {
        View content = findViewById(R.id.etUsername).getParent().getParent().getParent();
        content.setAlpha(0f);
        content.animate()
            .alpha(1f)
            .setDuration(300)
            .setInterpolator(new android.view.animation.DecelerateInterpolator())
            .start();
    }

    private void loadSavedCreds() {
        if (etUsername != null) etUsername.setText(prefs.getString("username", ""));
        if (etToken != null) etToken.setText(prefs.getString("token", ""));
        if (etRepo != null) etRepo.setText(prefs.getString("repo", ""));
        if (cbSaveCreds != null) cbSaveCreds.setChecked(prefs.getBoolean("save", false));
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
        showToast("Credentials cleared");
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
                showStatus("Error reading file: " + e.getMessage(), true);
            }
        }
    }

    private void commitAndPush() {
        String username = etUsername.getText().toString().trim();
        String token = etToken.getText().toString().trim();
        String repo = etRepo.getText().toString().trim();
        String filePath = etFilePath.getText().toString().trim();
        String content = etContent.getText().toString();
        String message = etMessage.getText().toString().trim();

        if (username.isEmpty() || token.isEmpty() || repo.isEmpty() || filePath.isEmpty()) {
            showStatus("Please fill all required fields", true);
            return;
        }

        if (selectedFileContent != null && !selectedFileContent.isEmpty()) {
            content = selectedFileContent;
        }

        if (content.isEmpty()) {
            showStatus("No content to commit", true);
            return;
        }

        saveCreds();
        showStatus("Processing...", false);
        
        String finalContent = content;
        executor.execute(() -> {
            GitHubAPI api = new GitHubAPI(username, token, repo);
            String result = api.commitAndPush(filePath, finalContent, message);
            runOnUiThread(() -> {
                boolean isError = !result.contains("Success");
                showStatus(result, isError);
                if (!isError) {
                    etContent.setText("");
                    etMessage.setText("");
                    tvSelectedFile.setText("");
                    selectedFileContent = null;
                }
            });
        });
    }

    private void showStatus(String message, boolean isError) {
        tvStatus.setText(message);
        tvStatus.setTextColor(getResources().getColor(
            isError ? R.color.error : R.color.success, getTheme()));
        statusCard.setVisibility(View.VISIBLE);
        statusCard.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_up));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
