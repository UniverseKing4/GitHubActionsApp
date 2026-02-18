package com.github.actions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;

public class ProjectsActivity extends AppCompatActivity {
    private LinearLayout projectsList;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Apply dark mode
        SharedPreferences themePrefs = getSharedPreferences("GitCodeTheme", MODE_PRIVATE);
        boolean isDark = themePrefs.getBoolean("darkMode", false);
        
        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        prefs = getSharedPreferences("GitCodeProjects", MODE_PRIVATE);
        
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(20, 20, 20, 20);
        if (isDark) {
            mainLayout.setBackgroundColor(0xFF1E1E1E);
        }
        
        TextView title = new TextView(this);
        title.setText("GitCode");
        title.setTextSize(32);
        title.setTextColor(0xFF4CAF50);
        title.setPadding(0, 20, 0, 40);
        mainLayout.addView(title);
        
        Button btnNew = new Button(this);
        btnNew.setText("+ New Project");
        btnNew.setOnClickListener(v -> createProject());
        mainLayout.addView(btnNew);
        
        Button btnSettings = new Button(this);
        btnSettings.setText("⚙ GitHub Settings");
        btnSettings.setOnClickListener(v -> showSettings());
        mainLayout.addView(btnSettings);
        
        Button btnDarkMode = new Button(this);
        btnDarkMode.setText(isDark ? "☀ Light Mode" : "🌙 Dark Mode");
        btnDarkMode.setOnClickListener(v -> {
            boolean currentDark = themePrefs.getBoolean("darkMode", false);
            themePrefs.edit().putBoolean("darkMode", !currentDark).apply();
            recreate();
        });
        mainLayout.addView(btnDarkMode);
        
        TextView projectsTitle = new TextView(this);
        projectsTitle.setText("Recent Projects");
        projectsTitle.setTextSize(20);
        projectsTitle.setPadding(0, 40, 0, 20);
        if (isDark) {
            projectsTitle.setTextColor(0xFFFFFFFF);
        }
        mainLayout.addView(projectsTitle);
        
        projectsList = new LinearLayout(this);
        projectsList.setOrientation(LinearLayout.VERTICAL);
        mainLayout.addView(projectsList);
        
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(mainLayout);
        setContentView(scrollView);
        
        loadProjects();
    }

    private void createProject() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Project");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        
        EditText etName = new EditText(this);
        etName.setHint("Project Name");
        layout.addView(etName);
        
        EditText etPath = new EditText(this);
        etPath.setHint("Path (optional)");
        etPath.setText(Environment.getExternalStorageDirectory() + "/GitCode/");
        layout.addView(etPath);
        
        builder.setView(layout);
        builder.setPositiveButton("Create", (d, w) -> {
            String name = etName.getText().toString().trim();
            String path = etPath.getText().toString().trim();
            
            if (name.isEmpty()) {
                Toast.makeText(this, "Enter project name", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (path.isEmpty()) {
                path = Environment.getExternalStorageDirectory() + "/GitCode/";
            }
            
            if (!path.endsWith("/")) {
                path += "/";
            }
            
            String fullPath = path + name;
            File dir = new File(fullPath);
            
            if (dir.exists()) {
                Toast.makeText(this, "Project already exists", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (dir.mkdirs()) {
                // Use the actual created folder name
                String actualName = dir.getName();
                saveProject(actualName, fullPath);
                openProject(actualName, fullPath);
            } else {
                Toast.makeText(this, "Failed to create project", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void saveProject(String name, String path) {
        String projects = prefs.getString("projects", "");
        projects += name + "|" + path + ";";
        prefs.edit().putString("projects", projects).apply();
    }

    private void loadProjects() {
        projectsList.removeAllViews();
        String projects = prefs.getString("projects", "");
        
        if (projects.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No projects yet. Create one to get started!");
            empty.setTextColor(0xFF666666);
            projectsList.addView(empty);
            return;
        }
        
        String[] projectArray = projects.split(";");
        StringBuilder validProjects = new StringBuilder();
        
        for (String project : projectArray) {
            if (project.isEmpty()) continue;
            String[] parts = project.split("\\|");
            if (parts.length != 2) continue;
            
            String name = parts[0];
            String path = parts[1];
            
            File dir = new File(path);
            if (!dir.exists()) continue;
            
            // Get actual folder name from path
            String actualName = dir.getName();
            
            validProjects.append(actualName).append("|").append(path).append(";");
            
            LinearLayout projectItem = new LinearLayout(this);
            projectItem.setOrientation(LinearLayout.HORIZONTAL);
            projectItem.setPadding(0, 5, 0, 5);
            
            Button btn = new Button(this);
            btn.setText("📁 " + actualName);
            btn.setTransformationMethod(null); // Prevent auto-capitalization
            btn.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            btn.setOnClickListener(v -> openProject(actualName, path));
            projectItem.addView(btn);
            
            Button btnEdit = new Button(this);
            btnEdit.setText("✏");
            btnEdit.setOnClickListener(v -> editProject(actualName, path));
            projectItem.addView(btnEdit);
            
            Button btnDelete = new Button(this);
            btnDelete.setText("🗑");
            btnDelete.setOnClickListener(v -> deleteProject(actualName, path));
            projectItem.addView(btnDelete);
            
            projectsList.addView(projectItem);
        }
        
        prefs.edit().putString("projects", validProjects.toString()).apply();
    }

    private void editProject(String oldName, String path) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Project Name");
        EditText input = new EditText(this);
        input.setText(oldName);
        input.setPadding(50, 20, 50, 20);
        builder.setView(input);
        builder.setPositiveButton("Save", (d, w) -> {
            String newName = input.getText().toString().trim();
            if (newName.isEmpty() || newName.equals(oldName)) return;
            
            String projects = prefs.getString("projects", "");
            projects = projects.replace(oldName + "|" + path, newName + "|" + path);
            prefs.edit().putString("projects", projects).apply();
            loadProjects();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void deleteProject(String name, String path) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Project");
        builder.setMessage("Delete '" + name + "' and all its files?");
        builder.setPositiveButton("Delete", (d, w) -> {
            File dir = new File(path);
            if (deleteRecursive(dir)) {
                String projects = prefs.getString("projects", "");
                projects = projects.replace(name + "|" + path + ";", "");
                prefs.edit().putString("projects", projects).apply();
                loadProjects();
                Toast.makeText(this, "Project deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to delete project", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private boolean deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteRecursive(f);
                }
            }
        }
        return file.delete();
    }

    private void openProject(String name, String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            Toast.makeText(this, "Project folder not found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent intent = new Intent(this, IDEActivity.class);
        intent.putExtra("projectName", name);
        intent.putExtra("projectPath", path);
        startActivity(intent);
    }

    private void showSettings() {
        SharedPreferences creds = getSharedPreferences("GitHubCreds", MODE_PRIVATE);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("GitHub Settings");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        EditText etUser = new EditText(this);
        etUser.setHint("Username");
        etUser.setText(creds.getString("username", ""));
        layout.addView(etUser);

        EditText etToken = new EditText(this);
        etToken.setHint("Token");
        etToken.setText(creds.getString("token", ""));
        etToken.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etToken);

        builder.setView(layout);
        builder.setPositiveButton("Save", (d, w) -> {
            creds.edit()
                .putString("username", etUser.getText().toString())
                .putString("token", etToken.getText().toString())
                .apply();
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProjects();
    }
}
