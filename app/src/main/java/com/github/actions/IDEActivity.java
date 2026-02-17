package com.github.actions;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IDEActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private LinearLayout fileList;
    private EditText editor;
    private String projectName, projectPath;
    private File currentFile;
    private SharedPreferences prefs;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        projectName = getIntent().getStringExtra("projectName");
        projectPath = getIntent().getStringExtra("projectPath");
        prefs = getSharedPreferences("GitHubCreds", MODE_PRIVATE);
        
        // Verify project path exists
        File projectDir = new File(projectPath);
        if (!projectDir.exists()) {
            Toast.makeText(this, "Project folder not found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        drawerLayout = new DrawerLayout(this);
        
        // Main editor area
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setLayoutParams(new DrawerLayout.LayoutParams(
            DrawerLayout.LayoutParams.MATCH_PARENT,
            DrawerLayout.LayoutParams.MATCH_PARENT));
        
        ScrollView editorScroll = new ScrollView(this);
        editor = new EditText(this);
        editor.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editor.setTypeface(android.graphics.Typeface.MONOSPACE);
        editor.setTextSize(14);
        editor.setGravity(Gravity.TOP | Gravity.START);
        editor.setPadding(20, 20, 20, 20);
        editor.setHorizontallyScrolling(true);
        editor.setBackgroundColor(0xFFFFFFFF);
        editorScroll.addView(editor);
        mainLayout.addView(editorScroll);
        
        // File drawer
        LinearLayout drawer = new LinearLayout(this);
        drawer.setOrientation(LinearLayout.VERTICAL);
        drawer.setBackgroundColor(0xFFF5F5F5);
        drawer.setLayoutParams(new DrawerLayout.LayoutParams(
            (int)(300 * getResources().getDisplayMetrics().density),
            DrawerLayout.LayoutParams.MATCH_PARENT,
            Gravity.START));
        
        TextView drawerTitle = new TextView(this);
        drawerTitle.setText("Files");
        drawerTitle.setTextSize(20);
        drawerTitle.setPadding(20, 40, 20, 10);
        drawer.addView(drawerTitle);
        
        LinearLayout btnContainer = new LinearLayout(this);
        btnContainer.setOrientation(LinearLayout.HORIZONTAL);
        btnContainer.setPadding(10, 0, 10, 10);
        
        android.widget.Button btnNewFile = new android.widget.Button(this);
        btnNewFile.setText("+ File");
        btnNewFile.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        btnNewFile.setOnClickListener(v -> createNewFile());
        btnContainer.addView(btnNewFile);
        
        android.widget.Button btnNewFolder = new android.widget.Button(this);
        btnNewFolder.setText("+ Folder");
        btnNewFolder.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        btnNewFolder.setOnClickListener(v -> createNewFolder());
        btnContainer.addView(btnNewFolder);
        
        drawer.addView(btnContainer);
        
        ScrollView fileScroll = new ScrollView(this);
        fileList = new LinearLayout(this);
        fileList.setOrientation(LinearLayout.VERTICAL);
        fileList.setPadding(10, 10, 10, 10);
        fileScroll.addView(fileList);
        drawer.addView(fileScroll);
        
        drawerLayout.addView(mainLayout);
        drawerLayout.addView(drawer);
        setContentView(drawerLayout);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(projectName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(android.R.drawable.ic_menu_sort_by_size);
        }
        
        loadFiles();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Save");
        menu.add(0, 2, 0, "Commit & Push All");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (drawerLayout.isDrawerOpen(Gravity.START)) {
                    drawerLayout.closeDrawer(Gravity.START);
                } else {
                    drawerLayout.openDrawer(Gravity.START);
                }
                return true;
            case 1:
                saveCurrentFile();
                return true;
            case 2:
                commitAndPushAll();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadFiles() {
        loadFiles(new File(projectPath), fileList);
    }

    private void loadFiles(File dir, LinearLayout container) {
        container.removeAllViews();
        if (!dir.exists()) dir.mkdirs();
        
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            TextView empty = new TextView(this);
            empty.setText("No files yet");
            empty.setPadding(20, 20, 20, 20);
            container.addView(empty);
            return;
        }
        
        for (File file : files) {
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setPadding(5, 5, 5, 5);
            
            TextView tv = new TextView(this);
            if (file.isDirectory()) {
                tv.setText("📁 " + file.getName());
                tv.setOnClickListener(v -> expandFolder(file, container));
            } else {
                tv.setText("📄 " + file.getName());
                tv.setOnClickListener(v -> openFile(file));
            }
            tv.setPadding(15, 10, 15, 10);
            tv.setTextSize(16);
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            itemLayout.addView(tv);
            
            TextView btnMenu = new TextView(this);
            btnMenu.setText("⋮");
            btnMenu.setTextSize(20);
            btnMenu.setPadding(10, 0, 10, 0);
            btnMenu.setOnClickListener(v -> showFileMenu(file));
            itemLayout.addView(btnMenu);
            
            container.addView(itemLayout);
        }
    }

    private void expandFolder(File folder, LinearLayout container) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(folder.getName());
        
        ScrollView scroll = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);
        
        loadFiles(folder, layout);
        
        scroll.addView(layout);
        builder.setView(scroll);
        builder.setNegativeButton("Close", null);
        builder.show();
    }

    private void showFileMenu(File file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(file.getName());
        
        String[] options = file.isDirectory() ? 
            new String[]{"Rename", "Delete"} : 
            new String[]{"Open", "Rename", "Delete"};
        
        builder.setItems(options, (d, which) -> {
            if (file.isDirectory()) {
                switch (which) {
                    case 0: renameFile(file); break;
                    case 1: deleteFile(file); break;
                }
            } else {
                switch (which) {
                    case 0: openFile(file); break;
                    case 1: renameFile(file); break;
                    case 2: deleteFile(file); break;
                }
            }
        });
        builder.show();
    }

    private void renameFile(File file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename");
        EditText input = new EditText(this);
        input.setText(file.getName());
        input.setPadding(50, 20, 50, 20);
        builder.setView(input);
        builder.setPositiveButton("Rename", (d, w) -> {
            String newName = input.getText().toString().trim();
            if (newName.isEmpty()) return;
            
            File newFile = new File(file.getParent(), newName);
            if (file.renameTo(newFile)) {
                if (currentFile != null && currentFile.equals(file)) {
                    currentFile = newFile;
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setSubtitle(newName);
                    }
                }
                loadFiles();
                Toast.makeText(this, "Renamed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to rename", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void deleteFile(File file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete");
        builder.setMessage("Delete " + file.getName() + "?");
        builder.setPositiveButton("Delete", (d, w) -> {
            if (deleteRecursive(file)) {
                if (currentFile != null && currentFile.equals(file)) {
                    currentFile = null;
                    editor.setText("");
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setSubtitle("");
                    }
                }
                loadFiles();
                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
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

    private void createNewFolder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Folder");
        EditText input = new EditText(this);
        input.setHint("folder name");
        input.setPadding(50, 20, 50, 20);
        builder.setView(input);
        builder.setPositiveButton("Create", (d, w) -> {
            String name = input.getText().toString().trim();
            if (name.isEmpty()) return;
            
            File folder = new File(projectPath, name);
            if (folder.mkdirs()) {
                loadFiles();
                Toast.makeText(this, "Folder created", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to create folder", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void openFile(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            
            currentFile = file;
            String content = new String(data);
            editor.setText(content);
            
            if (getSupportActionBar() != null) {
                getSupportActionBar().setSubtitle(file.getName());
            }
            drawerLayout.closeDrawer(Gravity.START);
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void applySyntaxHighlighting(String fileName, String content) {
        if (content == null || content.isEmpty()) return;
        
        String ext = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            ext = fileName.substring(i + 1).toLowerCase();
        }
        
        android.text.SpannableStringBuilder spannable = new android.text.SpannableStringBuilder(content);
        
        String[] keywords = getKeywordsForExtension(ext);
        int keywordColor = 0xFF0000FF;
        int stringColor = 0xFF008000;
        int commentColor = 0xFF808080;
        
        try {
            // Highlight keywords
            for (String keyword : keywords) {
                int start = 0;
                while ((start = content.indexOf(keyword, start)) != -1) {
                    if ((start == 0 || !Character.isLetterOrDigit(content.charAt(start - 1))) &&
                        (start + keyword.length() >= content.length() || !Character.isLetterOrDigit(content.charAt(start + keyword.length())))) {
                        spannable.setSpan(new android.text.style.ForegroundColorSpan(keywordColor),
                            start, start + keyword.length(),
                            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    start += keyword.length();
                }
            }
            
            // Highlight strings
            highlightPattern(spannable, content, "\"[^\"]*\"", stringColor);
            highlightPattern(spannable, content, "'[^']*'", stringColor);
            
            // Highlight comments
            highlightPattern(spannable, content, "//.*", commentColor);
            highlightPattern(spannable, content, "/\\*.*?\\*/", commentColor);
            highlightPattern(spannable, content, "#.*", commentColor);
            
            int selection = editor.getSelectionStart();
            editor.setText(spannable);
            if (selection >= 0 && selection <= editor.getText().length()) {
                editor.setSelection(selection);
            }
        } catch (Exception e) {
            // Fallback to plain text if highlighting fails
            editor.setText(content);
        }
    }

    private void highlightPattern(android.text.SpannableStringBuilder spannable, String content, String regex, int color) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            spannable.setSpan(new android.text.style.ForegroundColorSpan(color),
                matcher.start(), matcher.end(),
                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private String[] getKeywordsForExtension(String ext) {
        switch (ext) {
            case "java":
                return new String[]{"public", "private", "protected", "class", "interface", "extends", "implements", 
                    "void", "int", "String", "boolean", "double", "float", "long", "short", "byte", "char",
                    "if", "else", "for", "while", "do", "switch", "case", "break", "continue", "return", 
                    "new", "this", "super", "static", "final", "abstract", "synchronized", "volatile",
                    "try", "catch", "throw", "throws", "finally", "import", "package", "enum", "null", "true", "false"};
            case "js":
            case "jsx":
            case "ts":
            case "tsx":
                return new String[]{"function", "const", "let", "var", "if", "else", "for", "while", "do", "switch", 
                    "case", "break", "continue", "return", "class", "extends", "implements", "interface",
                    "import", "export", "default", "async", "await", "try", "catch", "throw", "finally",
                    "new", "this", "typeof", "instanceof", "null", "undefined", "true", "false"};
            case "py":
                return new String[]{"def", "class", "if", "elif", "else", "for", "while", "return", "import", 
                    "from", "as", "try", "except", "finally", "with", "lambda", "yield", "async", "await",
                    "pass", "break", "continue", "raise", "assert", "del", "global", "nonlocal",
                    "True", "False", "None", "and", "or", "not", "in", "is"};
            case "html":
            case "xml":
                return new String[]{"div", "span", "html", "head", "body", "script", "style", "link", "meta", 
                    "title", "h1", "h2", "h3", "h4", "h5", "h6", "p", "a", "img", "button", "input", "form",
                    "table", "tr", "td", "th", "ul", "ol", "li", "nav", "header", "footer", "section", "article"};
            case "css":
            case "scss":
            case "sass":
                return new String[]{"color", "background", "margin", "padding", "border", "width", "height", 
                    "display", "flex", "grid", "position", "top", "left", "right", "bottom",
                    "font", "text", "align", "justify", "transform", "transition", "animation"};
            case "c":
            case "cpp":
            case "h":
            case "hpp":
                return new String[]{"int", "char", "float", "double", "void", "long", "short", "unsigned", "signed",
                    "if", "else", "for", "while", "do", "switch", "case", "break", "continue", "return",
                    "struct", "union", "enum", "typedef", "sizeof", "const", "static", "extern",
                    "include", "define", "ifdef", "ifndef", "endif", "NULL", "true", "false"};
            case "go":
                return new String[]{"func", "var", "const", "type", "struct", "interface", "package", "import",
                    "if", "else", "for", "switch", "case", "break", "continue", "return", "defer", "go",
                    "chan", "select", "range", "map", "make", "new", "nil", "true", "false"};
            case "rs":
                return new String[]{"fn", "let", "mut", "const", "static", "struct", "enum", "trait", "impl",
                    "if", "else", "for", "while", "loop", "match", "break", "continue", "return",
                    "pub", "use", "mod", "crate", "self", "super", "true", "false", "None", "Some"};
            case "php":
                return new String[]{"function", "class", "interface", "trait", "extends", "implements",
                    "public", "private", "protected", "static", "final", "abstract",
                    "if", "else", "elseif", "for", "foreach", "while", "do", "switch", "case", "break", "continue", "return",
                    "try", "catch", "throw", "finally", "new", "this", "self", "parent",
                    "true", "false", "null", "echo", "print", "var", "const"};
            case "rb":
                return new String[]{"def", "class", "module", "if", "elsif", "else", "unless", "case", "when",
                    "for", "while", "until", "break", "next", "return", "yield", "begin", "rescue", "ensure", "end",
                    "true", "false", "nil", "self", "super", "require", "include", "attr_accessor"};
            case "swift":
                return new String[]{"func", "var", "let", "class", "struct", "enum", "protocol", "extension",
                    "if", "else", "guard", "switch", "case", "for", "while", "repeat", "break", "continue", "return",
                    "import", "public", "private", "internal", "static", "final", "override",
                    "true", "false", "nil", "self", "super", "try", "catch", "throw"};
            case "kt":
                return new String[]{"fun", "val", "var", "class", "interface", "object", "companion",
                    "if", "else", "when", "for", "while", "do", "break", "continue", "return",
                    "public", "private", "protected", "internal", "open", "abstract", "final", "override",
                    "true", "false", "null", "this", "super", "import", "package"};
            default:
                return new String[]{};
        }
    }

    private void createNewFile() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New File");
        EditText input = new EditText(this);
        input.setHint("filename.ext");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(50, 20, 50, 20);
        input.setLayoutParams(lp);
        builder.setView(input);
        builder.setPositiveButton("Create", (d, w) -> {
            String name = input.getText().toString().trim();
            if (name.isEmpty()) return;
            
            File file = new File(projectPath, name);
            try {
                file.createNewFile();
                currentFile = file;
                editor.setText("");
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setSubtitle(name);
                }
                loadFiles();
            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void saveCurrentFile() {
        if (currentFile == null) {
            Toast.makeText(this, "No file open", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            FileOutputStream fos = new FileOutputStream(currentFile);
            fos.write(editor.getText().toString().getBytes());
            fos.close();
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void commitAndPushAll() {
        String username = prefs.getString("username", "");
        String token = prefs.getString("token", "");
        
        if (username.isEmpty() || token.isEmpty()) {
            Toast.makeText(this, "Configure GitHub settings first", Toast.LENGTH_LONG).show();
            return;
        }
        
        if (currentFile != null) {
            saveCurrentFile();
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Commit & Push All Files");
        EditText input = new EditText(this);
        input.setHint("Commit message");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(50, 20, 50, 20);
        input.setLayoutParams(lp);
        builder.setView(input);
        builder.setPositiveButton("Push", (d, w) -> {
            String message = input.getText().toString();
            if (message.isEmpty()) message = "Update project";
            pushAllToGitHub(username, token, projectName, message);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void pushAllToGitHub(String username, String token, String repo, String message) {
        Toast.makeText(this, "Pushing all files to GitHub...", Toast.LENGTH_SHORT).show();
        executor.execute(() -> {
            GitHubAPI api = new GitHubAPI(username, token, repo);
            File dir = new File(projectPath);
            File[] files = dir.listFiles();
            
            if (files == null || files.length == 0) {
                runOnUiThread(() -> Toast.makeText(this, "No files to push", Toast.LENGTH_SHORT).show());
                return;
            }
            
            int success = 0;
            for (File file : files) {
                if (file.isFile()) {
                    try {
                        FileInputStream fis = new FileInputStream(file);
                        byte[] data = new byte[(int) file.length()];
                        fis.read(data);
                        fis.close();
                        
                        String content = new String(data);
                        String result = api.commitAndPush(file.getName(), content, message);
                        if (result.contains("Success")) {
                            success++;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            
            int finalSuccess = success;
            runOnUiThread(() -> Toast.makeText(this, "Pushed " + finalSuccess + " files successfully", Toast.LENGTH_LONG).show());
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
