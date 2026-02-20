package com.github.actions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
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
        boolean isDark = themePrefs.getBoolean("darkMode", true);
        
        // Dark navigation bar
        if (isDark && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(0xFF000000);
        }
        
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
        title.setPadding(0, 20, 0, 2);
        mainLayout.addView(title);
        
        View underline = new View(this);
        underline.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            (int)(2 * getResources().getDisplayMetrics().density)));
        underline.setBackgroundColor(isDark ? 0xFFFFFFFF : 0xFF000000);
        title.post(() -> {
            android.graphics.Paint paint = title.getPaint();
            int textWidth = (int) paint.measureText("GitCode");
            underline.getLayoutParams().width = textWidth;
            underline.requestLayout();
        });
        mainLayout.addView(underline);
        
        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            (int)(38 * getResources().getDisplayMetrics().density)));
        mainLayout.addView(spacer);
        
        Button btnNew = new Button(this);
        btnNew.setText("+ New Project");
        btnNew.setOnClickListener(v -> createProject());
        mainLayout.addView(btnNew);
        
        Button btnClone = new Button(this);
        btnClone.setText("📥 Clone from GitHub");
        btnClone.setOnClickListener(v -> cloneRepo());
        mainLayout.addView(btnClone);
        
        Button btnProfiles = new Button(this);
        btnProfiles.setText("👤 GitHub Profiles");
        btnProfiles.setOnClickListener(v -> showProfiles());
        mainLayout.addView(btnProfiles);
        
        Button btnSettings = new Button(this);
        btnSettings.setText("⚙ Settings");
        btnSettings.setOnClickListener(v -> showSettings());
        mainLayout.addView(btnSettings);
        
        TextView projectsTitle = new TextView(this);
        projectsTitle.setText("Projects");
        projectsTitle.setTextSize(20);
        projectsTitle.setPadding(0, 40, 0, 20);
        projectsTitle.setTextColor(isDark ? 0xFFFFFFFF : 0xFF000000);
        mainLayout.addView(projectsTitle);
        
        projectsList = new LinearLayout(this);
        projectsList.setOrientation(LinearLayout.VERTICAL);
        mainLayout.addView(projectsList);
        
        ScrollView scrollView = new ScrollView(this);
        if (isDark) {
            scrollView.setBackgroundColor(0xFF1E1E1E);
        }
        scrollView.addView(mainLayout);
        setContentView(scrollView);
        
        loadProjects();
    }
    
    private AlertDialog.Builder createThemedDialog() {
        return new AlertDialog.Builder(this);
    }

    private void createProject() {
        AlertDialog.Builder builder = createThemedDialog();
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
            
            validProjects.append(name).append("|").append(path).append(";");
            
            LinearLayout projectItem = new LinearLayout(this);
            projectItem.setOrientation(LinearLayout.HORIZONTAL);
            projectItem.setPadding(0, 5, 0, 5);
            
            Button btn = new Button(this);
            btn.setText("📁 " + name);
            btn.setTransformationMethod(null); // Prevent auto-capitalization
            btn.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            btn.setOnClickListener(v -> openProject(name, path));
            projectItem.addView(btn);
            
            Button btnEdit = new Button(this);
            btnEdit.setText("✏️");
            btnEdit.setTextSize(14);
            btnEdit.setMinWidth(0);
            btnEdit.setMinimumWidth(0);
            btnEdit.setPadding(20, 0, 20, 0);
            btnEdit.setOnClickListener(v -> editProject(name, path));
            projectItem.addView(btnEdit);
            
            Button btnDelete = new Button(this);
            btnDelete.setText("🗑");
            btnDelete.setTextSize(14);
            btnDelete.setMinWidth(0);
            btnDelete.setMinimumWidth(0);
            btnDelete.setPadding(20, 0, 20, 0);
            btnDelete.setOnClickListener(v -> deleteProject(name, path));
            projectItem.addView(btnDelete);
            
            projectsList.addView(projectItem);
        }
        
        prefs.edit().putString("projects", validProjects.toString()).apply();
    }

    private void editProject(String oldName, String path) {
        AlertDialog.Builder builder = createThemedDialog();
        builder.setTitle("Edit Project Name");
        EditText input = new EditText(this);
        input.setText(oldName);
        input.setPadding(50, 20, 50, 20);
        builder.setView(input);
        builder.setPositiveButton("Save", (d, w) -> {
            String newName = input.getText().toString().trim();
            if (newName.isEmpty() || newName.equals(oldName)) return;
            
            // Check if project with new name already exists
            File gitCodeDir = new File(Environment.getExternalStorageDirectory(), "GitCode");
            File newDir = new File(gitCodeDir, newName);
            if (newDir.exists() && !newDir.getAbsolutePath().equals(path)) {
                Toast.makeText(this, "Another project has same name", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Rename the actual folder
            File oldDir = new File(path);
            File targetDir = new File(oldDir.getParent(), newName);
            
            if (oldDir.renameTo(targetDir)) {
                String newPath = targetDir.getAbsolutePath();
                String projects = prefs.getString("projects", "");
                projects = projects.replace(oldName + "|" + path, newName + "|" + newPath);
                prefs.edit().putString("projects", projects).apply();
                Toast.makeText(this, "Project renamed", Toast.LENGTH_SHORT).show();
                loadProjects();
            } else {
                Toast.makeText(this, "Failed to rename project", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void deleteProject(String name, String path) {
        AlertDialog.Builder builder = createThemedDialog();
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

    private void showProfiles() {
        SharedPreferences profilePrefs = getSharedPreferences("GitHubProfiles", MODE_PRIVATE);
        String profiles = profilePrefs.getString("profiles", "");
        String activeProfile = profilePrefs.getString("activeProfile", "");
        
        AlertDialog.Builder builder = createThemedDialog();
        builder.setTitle("GitHub Profiles");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);
        
        if (profiles.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No profiles yet");
            empty.setPadding(20, 20, 20, 20);
            layout.addView(empty);
        } else {
            for (String profile : profiles.split(";")) {
                if (profile.isEmpty()) continue;
                String[] parts = profile.split("\\|");
                if (parts.length != 2) continue;
                
                String username = parts[0];
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                
                Button btn = new Button(this);
                btn.setText((username.equals(activeProfile) ? "✓ " : "") + username);
                btn.setAllCaps(false); // Preserve case sensitivity
                btn.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                row.addView(btn);
                
                Button btnDel = new Button(this);
                btnDel.setText("🗑");
                btnDel.setOnClickListener(v -> {
                    String newProfiles = profiles.replace(profile + ";", "");
                    profilePrefs.edit().putString("profiles", newProfiles).apply();
                    if (username.equals(activeProfile)) {
                        profilePrefs.edit().remove("activeProfile").apply();
                    }
                    showProfiles();
                });
                row.addView(btnDel);
                
                layout.addView(row);
            }
        }
        
        Button btnAdd = new Button(this);
        btnAdd.setText("+ Add Profile");
        layout.addView(btnAdd);
        
        ScrollView scroll = new ScrollView(this);
        scroll.addView(layout);
        builder.setView(scroll);
        builder.setNegativeButton("Close", null);
        AlertDialog dialog = builder.create();
        
        // Set click listeners after dialog is created
        for (String profile : profiles.split(";")) {
            if (profile.isEmpty()) continue;
            String[] parts = profile.split("\\|");
            if (parts.length != 2) continue;
            String username = parts[0];
            
            for (int i = 0; i < layout.getChildCount(); i++) {
                android.view.View child = layout.getChildAt(i);
                if (child instanceof LinearLayout) {
                    LinearLayout row = (LinearLayout) child;
                    if (row.getChildCount() >= 2) {
                        if (row.getChildAt(0) instanceof Button) {
                            Button btn = (Button) row.getChildAt(0);
                            String btnText = btn.getText().toString().replace("✓ ", "");
                            if (btnText.equals(username)) {
                                btn.setOnClickListener(v -> {
                                    profilePrefs.edit().putString("activeProfile", username).apply();
                                    SharedPreferences creds = getSharedPreferences("GitHubCreds", MODE_PRIVATE);
                                    creds.edit()
                                        .putString("username", parts[0])
                                        .putString("token", parts[1])
                                        .apply();
                                    Toast.makeText(this, "Active: " + username, Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    showProfiles();
                                });
                            }
                        }
                        if (row.getChildAt(1) instanceof Button) {
                            Button btnDel = (Button) row.getChildAt(1);
                            btnDel.setOnClickListener(v -> {
                                createThemedDialog()
                                    .setTitle("Delete Profile")
                                    .setMessage("Delete profile: " + username + "?")
                                    .setPositiveButton("Delete", (d, w) -> {
                                        String newProfiles = profiles.replace(profile + ";", "");
                                        profilePrefs.edit().putString("profiles", newProfiles).apply();
                                        if (username.equals(activeProfile)) {
                                            profilePrefs.edit().remove("activeProfile").apply();
                                        }
                                        dialog.dismiss();
                                        showProfiles();
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .show();
                            });
                        }
                    }
                }
            }
        }
        
        btnAdd.setOnClickListener(v -> {
            dialog.dismiss();
            addProfile();
        });
        
        dialog.show();
    }

    private void addProfile() {
        AlertDialog.Builder builder = createThemedDialog();
        builder.setTitle("Add GitHub Profile");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        
        EditText etUser = new EditText(this);
        etUser.setHint("Username");
        layout.addView(etUser);
        
        EditText etToken = new EditText(this);
        etToken.setHint("Token");
        etToken.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etToken);
        
        builder.setView(layout);
        builder.setPositiveButton("Add", (d, w) -> {
            String user = etUser.getText().toString().trim();
            String token = etToken.getText().toString().trim();
            
            if (user.isEmpty() || token.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            
            SharedPreferences profilePrefs = getSharedPreferences("GitHubProfiles", MODE_PRIVATE);
            String profiles = profilePrefs.getString("profiles", "");
            profiles += user + "|" + token + ";";
            profilePrefs.edit().putString("profiles", profiles).apply();
            
            Toast.makeText(this, "Profile added", Toast.LENGTH_SHORT).show();
            showProfiles();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void cloneRepo() {
        SharedPreferences creds = getSharedPreferences("GitHubCreds", MODE_PRIVATE);
        String username = creds.getString("username", "");
        String token = creds.getString("token", "");
        
        if (username.isEmpty() || token.isEmpty()) {
            Toast.makeText(this, "Configure GitHub profile first", Toast.LENGTH_LONG).show();
            return;
        }
        
        AlertDialog.Builder builder = createThemedDialog();
        builder.setTitle("Clone Repository");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        
        EditText etUrl = new EditText(this);
        etUrl.setHint("Repository URL (e.g., https://github.com/user/repo)");
        layout.addView(etUrl);
        
        builder.setView(layout);
        builder.setPositiveButton("Clone", (d, w) -> {
            String url = etUrl.getText().toString().trim();
            if (url.isEmpty()) {
                Toast.makeText(this, "Enter repository URL", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Parse URL to get username and repo
            String[] parts = url.replace("https://github.com/", "").replace(".git", "").split("/");
            if (parts.length != 2) {
                Toast.makeText(this, "Invalid GitHub URL", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String repoOwner = parts[0];
            String repoName = parts[1];
            cloneRepoFromGitHub(repoOwner, token, repoName);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void cloneRepoFromGitHub(String username, String token, String repo) {
        Toast.makeText(this, "Cloning repository...", Toast.LENGTH_SHORT).show();
        
        new Thread(() -> {
            GitHubAPI api = new GitHubAPI(username, token, repo);
            
            // Check if repo exists
            String repoCheck = api.checkRepoExists();
            if (repoCheck.contains("Error: 404")) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Repository '" + repo + "' not found", Toast.LENGTH_LONG).show();
                });
                return;
            } else if (repoCheck.contains("Error")) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Failed to connect: " + repoCheck, Toast.LENGTH_LONG).show();
                });
                return;
            }
            
            // Create project folder
            String path = Environment.getExternalStorageDirectory() + "/GitCode/" + repo;
            File dir = new File(path);
            if (dir.exists()) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Project already exists", Toast.LENGTH_SHORT).show();
                });
                return;
            }
            
            dir.mkdirs();
            
            // Pull all files from repo
            java.util.List<String> files = api.getRepoTree();
            
            if (files.isEmpty()) {
                dir.delete();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Repository is empty or couldn't fetch files", Toast.LENGTH_LONG).show();
                });
                return;
            }
            
            int cloned = 0;
            
            for (String filePath : files) {
                String content = api.pullFile(filePath);
                if (content != null) {
                    File file = new File(path, filePath);
                    file.getParentFile().mkdirs();
                    try {
                        java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
                        fos.write(content.getBytes());
                        fos.close();
                        cloned++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            
            saveProject(repo, path);
            
            int finalCloned = cloned;
            runOnUiThread(() -> {
                if (finalCloned > 0) {
                    Toast.makeText(this, "✓ Cloned " + finalCloned + " files", Toast.LENGTH_SHORT).show();
                    loadProjects();
                    openProject(repo, path);
                } else {
                    Toast.makeText(this, "Failed to clone files", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProjects();
    }

    private void showSettings() {
        AlertDialog.Builder builder = createThemedDialog();
        builder.setTitle("App Settings");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        
        TextView label = new TextView(this);
        label.setText("Default Font Size:");
        layout.addView(label);
        
        SharedPreferences settingsPrefs = getSharedPreferences("GitCodeSettings", MODE_PRIVATE);
        int currentSize = settingsPrefs.getInt("fontSize", 14);
        
        android.widget.SeekBar seekBar = new android.widget.SeekBar(this);
        seekBar.setMax(20);
        seekBar.setProgress(currentSize - 10);
        
        TextView sizeLabel = new TextView(this);
        sizeLabel.setText("Size: " + currentSize + "sp");
        layout.addView(sizeLabel);
        
        seekBar.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                int size = progress + 10;
                sizeLabel.setText("Size: " + size + "sp");
            }
            public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}
            public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
        });
        layout.addView(seekBar);
        
        // Dark mode toggle
        TextView darkModeLabel = new TextView(this);
        darkModeLabel.setText("Theme:");
        darkModeLabel.setPadding(0, 30, 0, 10);
        layout.addView(darkModeLabel);
        
        Button btnDarkMode = new Button(this);
        SharedPreferences themePrefs = getSharedPreferences("GitCodeTheme", MODE_PRIVATE);
        boolean isDark = themePrefs.getBoolean("darkMode", false);
        btnDarkMode.setText(isDark ? "☀ Switch to Light Mode" : "🌙 Switch to Dark Mode");
        layout.addView(btnDarkMode);
        
        builder.setView(layout);
        builder.setPositiveButton("Save", (d, w) -> {
            int size = seekBar.getProgress() + 10;
            settingsPrefs.edit().putInt("fontSize", size).apply();
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", null);
        
        AlertDialog dialog = builder.create();
        
        btnDarkMode.setOnClickListener(v -> {
            boolean currentDark = themePrefs.getBoolean("darkMode", false);
            themePrefs.edit().putBoolean("darkMode", !currentDark).apply();
            dialog.dismiss();
            recreate();
        });
        
        dialog.show();
    }
}
