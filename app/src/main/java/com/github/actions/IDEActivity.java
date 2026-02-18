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
    private boolean selectionMode = false;
    private java.util.Set<File> selectedFiles = new java.util.HashSet<>();
    private android.os.Handler autoSaveHandler = new android.os.Handler();
    private Runnable autoSaveRunnable;
    private java.util.Stack<String> undoStack = new java.util.Stack<>();
    private java.util.Stack<String> redoStack = new java.util.Stack<>();
    private boolean isUndoRedo = false;
    private long lastEditTime = 0;
    private static final long UNDO_DELAY = 1000; // 1 second
    private boolean wordWrapEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        projectName = getIntent().getStringExtra("projectName");
        projectPath = getIntent().getStringExtra("projectPath");
        prefs = getSharedPreferences("GitHubCreds", MODE_PRIVATE);
        
        // Apply dark mode
        SharedPreferences themePrefs = getSharedPreferences("GitCodeTheme", MODE_PRIVATE);
        boolean isDark = themePrefs.getBoolean("darkMode", false);
        
        // Dark navigation bar
        if (isDark && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(0xFF000000);
        }
        
        // Verify project path exists
        File projectDir = new File(projectPath);
        if (!projectDir.exists()) {
            Toast.makeText(this, "Project folder not found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        drawerLayout = new DrawerLayout(this);
        if (isDark) {
            drawerLayout.setBackgroundColor(0xFF1E1E1E);
        }
        
        // Main editor area
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setLayoutParams(new DrawerLayout.LayoutParams(
            DrawerLayout.LayoutParams.MATCH_PARENT,
            DrawerLayout.LayoutParams.MATCH_PARENT));
        if (isDark) {
            mainLayout.setBackgroundColor(0xFF1E1E1E);
        }
        
        // Editor container
        LinearLayout editorContainer = new LinearLayout(this);
        editorContainer.setOrientation(LinearLayout.HORIZONTAL);
        editorContainer.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT));
        if (isDark) {
            editorContainer.setBackgroundColor(0xFF1E1E1E);
        }
        
        // Line numbers
        ScrollView lineNumberScroll = new ScrollView(this);
        lineNumberScroll.setVerticalScrollBarEnabled(false);
        LinearLayout.LayoutParams lineNumParams = new LinearLayout.LayoutParams(
            (int)(30 * getResources().getDisplayMetrics().density),
            LinearLayout.LayoutParams.MATCH_PARENT);
        lineNumberScroll.setLayoutParams(lineNumParams);
        
        TextView lineNumbers = new TextView(this);
        lineNumbers.setTypeface(android.graphics.Typeface.MONOSPACE);
        lineNumbers.setTextSize(14);
        lineNumbers.setGravity(Gravity.TOP | Gravity.END);
        lineNumbers.setPadding(5, 20, 8, 20);
        lineNumbers.setBackgroundColor(isDark ? 0xFF2D2D2D : 0xFFF5F5F5);
        lineNumbers.setTextColor(isDark ? 0xFF666666 : 0xFF999999);
        lineNumbers.setLineSpacing(0, 1.0f);
        lineNumbers.setMinHeight(0);
        lineNumbers.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
        lineNumberScroll.addView(lineNumbers);
        editorContainer.addView(lineNumberScroll);
        
        ScrollView editorScroll = new ScrollView(this);
        editorScroll.setVerticalScrollBarEnabled(false);
        LinearLayout.LayoutParams editorScrollParams = new LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            1);
        editorScroll.setLayoutParams(editorScrollParams);
        editorScroll.setFillViewport(true);
        
        editor = new EditText(this);
        editor.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT));
        editor.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        editor.setTypeface(android.graphics.Typeface.MONOSPACE);
        editor.setTextSize(14);
        editor.setLineSpacing(0, 1.0f);
        editor.setGravity(Gravity.TOP | Gravity.START);
        editor.setPadding(10, 20, 15, 20);
        editor.setHorizontallyScrolling(false); // Enable word wrap
        editor.setBackgroundColor(isDark ? 0xFF1E1E1E : 0xFFFFFFFF);
        editor.setTextColor(isDark ? 0xFFE0E0E0 : 0xFF000000);
        editor.setHighlightColor(0x6633B5E5);
        editor.setVerticalScrollBarEnabled(false);
        
        // Tab key support and keyboard shortcuts
        editor.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == android.view.KeyEvent.ACTION_DOWN) {
                // Tab key
                if (keyCode == android.view.KeyEvent.KEYCODE_TAB) {
                    int start = editor.getSelectionStart();
                    int end = editor.getSelectionEnd();
                    editor.getText().replace(Math.min(start, end), Math.max(start, end), "    ");
                    return true;
                }
                // Ctrl+S to save
                if (keyCode == android.view.KeyEvent.KEYCODE_S && event.isCtrlPressed()) {
                    saveCurrentFile();
                    return true;
                }
                // Ctrl+Z to undo
                if (keyCode == android.view.KeyEvent.KEYCODE_Z && event.isCtrlPressed()) {
                    undo();
                    return true;
                }
                // Ctrl+Y to redo
                if (keyCode == android.view.KeyEvent.KEYCODE_Y && event.isCtrlPressed()) {
                    redo();
                    return true;
                }
                // Ctrl+F to find
                if (keyCode == android.view.KeyEvent.KEYCODE_F && event.isCtrlPressed()) {
                    showFindDialog();
                    return true;
                }
            }
            return false;
        });
        
        // Auto-save setup
        autoSaveRunnable = () -> {
            if (currentFile != null) {
                autoSaveFile();
            }
        };
        
        // Auto-indent and auto-brackets
        editor.addTextChangedListener(new android.text.TextWatcher() {
            private String before = "";
            private int cursorPos = 0;
            private boolean isProcessing = false;
            
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                before = s.toString();
                cursorPos = start;
            }
            
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            public void afterTextChanged(android.text.Editable s) {
                if (isProcessing) return;
                isProcessing = true;
                
                String text = s.toString();
                
                // Update line numbers
                updateLineNumbers(lineNumbers, text);
                
                // Sync scrolling
                editorScroll.post(() -> {
                    int scrollY = editorScroll.getScrollY();
                    lineNumberScroll.scrollTo(0, scrollY);
                });
                
                // Trigger auto-save after 2 seconds of inactivity
                autoSaveHandler.removeCallbacks(autoSaveRunnable);
                autoSaveHandler.postDelayed(autoSaveRunnable, 2000);
                
                // Track for undo/redo with time-based grouping
                if (!isUndoRedo && !text.equals(before)) {
                    long currentTime = System.currentTimeMillis();
                    // Only save to undo stack if enough time has passed or significant change
                    if (currentTime - lastEditTime > UNDO_DELAY) {
                        if (!undoStack.isEmpty() && !undoStack.peek().equals(before)) {
                            undoStack.push(before);
                        } else if (undoStack.isEmpty()) {
                            undoStack.push(before);
                        }
                        if (undoStack.size() > 50) undoStack.remove(0);
                        redoStack.clear();
                    }
                    lastEditTime = currentTime;
                }
                
                int selection = editor.getSelectionStart();
                
                // Smart bracket handling - Enter between brackets
                if (text.length() > before.length() && selection > 0 && selection < text.length()) {
                    if (text.charAt(selection - 1) == '\n') {
                        char prevChar = selection > 1 ? text.charAt(selection - 2) : 0;
                        char nextChar = text.charAt(selection);
                        
                        if ((prevChar == '{' && nextChar == '}') ||
                            (prevChar == '[' && nextChar == ']') ||
                            (prevChar == '(' && nextChar == ')')) {
                            
                            // Get indent of previous line
                            String[] lines = text.substring(0, selection - 1).split("\n");
                            String lastLine = lines.length > 0 ? lines[lines.length - 1] : "";
                            int indent = 0;
                            for (char c : lastLine.toCharArray()) {
                                if (c == ' ' || c == '\t') indent++;
                                else break;
                            }
                            
                            String indentStr = new String(new char[indent + 4]).replace('\0', ' ');
                            String closeIndent = new String(new char[indent]).replace('\0', ' ');
                            
                            s.insert(selection, indentStr + "\n" + closeIndent);
                            editor.setSelection(selection + indentStr.length());
                            isProcessing = false;
                            return;
                        }
                    }
                }
                
                // Auto-indent on new line
                if (text.length() > before.length() && selection > 0 && text.charAt(selection - 1) == '\n') {
                    String[] lines = text.substring(0, selection - 1).split("\n");
                    if (lines.length > 0) {
                        String lastLine = lines[lines.length - 1];
                        int indent = 0;
                        for (char c : lastLine.toCharArray()) {
                            if (c == ' ' || c == '\t') indent++;
                            else break;
                        }
                        
                        // Extra indent after opening brace
                        if (lastLine.trim().endsWith("{") || lastLine.trim().endsWith(":")) {
                            indent += 4;
                        }
                        
                        if (indent > 0) {
                            String indentStr = new String(new char[indent]).replace('\0', ' ');
                            s.insert(selection, indentStr);
                            editor.setSelection(selection + indent);
                        }
                    }
                }
                
                // Auto-close brackets
                else if (text.length() > before.length() && selection > 0) {
                    char typed = text.charAt(selection - 1);
                    char closing = 0;
                    
                    switch (typed) {
                        case '(': closing = ')'; break;
                        case '[': closing = ']'; break;
                        case '{': closing = '}'; break;
                        case '"': closing = '"'; break;
                        case '\'': closing = '\''; break;
                        case '`': closing = '`'; break;
                        case '<': 
                            // Auto-close < for HTML/XML
                            if (currentFile != null && (currentFile.getName().endsWith(".html") || 
                                currentFile.getName().endsWith(".xml") || 
                                currentFile.getName().endsWith(".jsx") || 
                                currentFile.getName().endsWith(".tsx"))) {
                                closing = '>';
                            }
                            break;
                    }
                    
                    if (closing != 0) {
                        if (selection >= text.length() || !Character.isLetterOrDigit(text.charAt(selection))) {
                            s.insert(selection, String.valueOf(closing));
                            editor.setSelection(selection);
                        }
                    }
                    
                    // Auto-close tags for HTML/XML
                    if (typed == '>' && currentFile != null && 
                        (currentFile.getName().endsWith(".html") || 
                         currentFile.getName().endsWith(".xml") ||
                         currentFile.getName().endsWith(".jsx") ||
                         currentFile.getName().endsWith(".tsx"))) {
                        
                        // Find opening tag
                        int tagStart = text.lastIndexOf('<', selection - 2);
                        if (tagStart >= 0 && tagStart < selection - 1) {
                            String tag = text.substring(tagStart + 1, selection - 1).trim().split(" ")[0];
                            if (!tag.isEmpty() && !tag.startsWith("/") && !tag.endsWith("/") &&
                                !tag.matches("(br|hr|img|input|meta|link|area|base|col|embed|param|source|track|wbr)")) {
                                String closeTag = "</" + tag + ">";
                                s.insert(selection, closeTag);
                                editor.setSelection(selection);
                            }
                        }
                    }
                }
                
                isProcessing = false;
            }
        });
        
        editorScroll.addView(editor);
        editorContainer.addView(editorScroll);
        
        mainLayout.addView(editorContainer);
        
        // File drawer
        LinearLayout drawer = new LinearLayout(this);
        drawer.setOrientation(LinearLayout.VERTICAL);
        drawer.setBackgroundColor(isDark ? 0xFF2D2D2D : 0xFFF5F5F5);
        drawer.setLayoutParams(new DrawerLayout.LayoutParams(
            (int)(300 * getResources().getDisplayMetrics().density),
            DrawerLayout.LayoutParams.MATCH_PARENT,
            Gravity.START));
        
        TextView drawerTitle = new TextView(this);
        drawerTitle.setText("Files");
        drawerTitle.setTextSize(20);
        drawerTitle.setPadding(20, 40, 20, 10);
        if (isDark) {
            drawerTitle.setTextColor(0xFFFFFFFF);
        }
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
        
        android.widget.Button btnSelect = new android.widget.Button(this);
        btnSelect.setText("Select");
        btnSelect.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        btnSelect.setOnClickListener(v -> toggleSelectionMode());
        btnContainer.addView(btnSelect);
        
        android.widget.Button btnActions = new android.widget.Button(this);
        btnActions.setText("Actions");
        btnActions.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        btnActions.setVisibility(View.GONE);
        btnActions.setTag("actionsBtn");
        btnActions.setOnClickListener(v -> showSelectionMenu());
        btnContainer.addView(btnActions);
        
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
            if (isDark) {
                getSupportActionBar().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0xFF2D2D2D));
            }
            getSupportActionBar().setTitle(projectName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(android.R.drawable.ic_menu_sort_by_size);
            
            // Add custom view with toolbar buttons
            LinearLayout toolbarView = new LinearLayout(this);
            toolbarView.setOrientation(LinearLayout.HORIZONTAL);
            toolbarView.setGravity(Gravity.END);
            
            addCompactButton(toolbarView, "💾", v -> saveCurrentFile());
            addCompactButton(toolbarView, "↶", v -> undo());
            addCompactButton(toolbarView, "↷", v -> redo());
            addCompactButton(toolbarView, "🚀", v -> commitAndPushAll());
            
            getSupportActionBar().setCustomView(toolbarView);
            getSupportActionBar().setDisplayShowCustomEnabled(true);
        }
        
        loadFiles();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Find");
        menu.add(0, 2, 0, "Replace");
        menu.add(0, 3, 0, "Go to Line");
        menu.add(0, 4, 0, "Select All");
        menu.add(0, 5, 0, "Duplicate Line");
        menu.add(0, 6, 0, "Delete Line");
        menu.add(0, 7, 0, "Word Wrap: ON");
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
                showFindDialog();
                return true;
            case 2:
                showReplaceDialog();
                return true;
            case 3:
                showGoToLineDialog();
                return true;
            case 4:
                editor.selectAll();
                return true;
            case 5:
                duplicateLine();
                return true;
            case 6:
                deleteLine();
                return true;
            case 7:
                toggleWordWrap(item);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleWordWrap(MenuItem item) {
        wordWrapEnabled = !wordWrapEnabled;
        editor.setHorizontallyScrolling(!wordWrapEnabled);
        item.setTitle(wordWrapEnabled ? "Word Wrap: ON" : "Word Wrap: OFF");
        Toast.makeText(this, wordWrapEnabled ? "Word wrap enabled" : "Word wrap disabled", Toast.LENGTH_SHORT).show();
    }

    private void duplicateLine() {
        int start = editor.getSelectionStart();
        String text = editor.getText().toString();
        
        // Find current line
        int lineStart = text.lastIndexOf('\n', start - 1) + 1;
        int lineEnd = text.indexOf('\n', start);
        if (lineEnd == -1) lineEnd = text.length();
        
        String line = text.substring(lineStart, lineEnd);
        editor.getText().insert(lineEnd, "\n" + line);
        Toast.makeText(this, "Line duplicated", Toast.LENGTH_SHORT).show();
    }

    private void deleteLine() {
        int start = editor.getSelectionStart();
        String text = editor.getText().toString();
        
        // Find current line
        int lineStart = text.lastIndexOf('\n', start - 1) + 1;
        int lineEnd = text.indexOf('\n', start);
        if (lineEnd == -1) lineEnd = text.length();
        else lineEnd++; // Include newline
        
        editor.getText().delete(lineStart, lineEnd);
        Toast.makeText(this, "Line deleted", Toast.LENGTH_SHORT).show();
    }

    private void undo() {
        if (!undoStack.isEmpty()) {
            String current = editor.getText().toString();
            redoStack.push(current);
            String previous = undoStack.pop();
            isUndoRedo = true;
            editor.setText(previous);
            if (currentFile != null) {
                applySyntaxHighlighting(currentFile.getName(), previous);
            }
            editor.setSelection(Math.min(previous.length(), editor.getText().length()));
            isUndoRedo = false;
        } else {
            Toast.makeText(this, "Nothing to undo", Toast.LENGTH_SHORT).show();
        }
    }

    private void redo() {
        if (!redoStack.isEmpty()) {
            String current = editor.getText().toString();
            undoStack.push(current);
            String next = redoStack.pop();
            isUndoRedo = true;
            editor.setText(next);
            if (currentFile != null) {
                applySyntaxHighlighting(currentFile.getName(), next);
            }
            editor.setSelection(Math.min(next.length(), editor.getText().length()));
            isUndoRedo = false;
        } else {
            Toast.makeText(this, "Nothing to redo", Toast.LENGTH_SHORT).show();
        }
    }

    private void showFindDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Find");
        EditText input = new EditText(this);
        input.setHint("Search text");
        input.setPadding(50, 20, 50, 20);
        builder.setView(input);
        builder.setPositiveButton("Find", (d, w) -> {
            String search = input.getText().toString();
            if (!search.isEmpty()) {
                String text = editor.getText().toString();
                int start = editor.getSelectionStart();
                int index = text.indexOf(search, start);
                if (index < 0) {
                    index = text.indexOf(search);
                }
                if (index >= 0) {
                    editor.setSelection(index, index + search.length());
                    editor.requestFocus();
                    Toast.makeText(this, "Found at position " + index, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Not found", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showReplaceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Find & Replace");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        
        EditText findInput = new EditText(this);
        findInput.setHint("Find");
        layout.addView(findInput);
        
        EditText replaceInput = new EditText(this);
        replaceInput.setHint("Replace with");
        layout.addView(replaceInput);
        
        builder.setView(layout);
        builder.setPositiveButton("Replace All", (d, w) -> {
            String find = findInput.getText().toString();
            String replace = replaceInput.getText().toString();
            if (!find.isEmpty()) {
                String text = editor.getText().toString();
                int count = 0;
                int index = 0;
                while ((index = text.indexOf(find, index)) != -1) {
                    count++;
                    index += find.length();
                }
                
                if (count > 0) {
                    String newText = text.replace(find, replace);
                    editor.setText(newText);
                    Toast.makeText(this, "Replaced " + count + " occurrences", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "No occurrences found", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showGoToLineDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Go to Line");
        EditText input = new EditText(this);
        input.setHint("Line number");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setPadding(50, 20, 50, 20);
        builder.setView(input);
        builder.setPositiveButton("Go", (d, w) -> {
            try {
                int line = Integer.parseInt(input.getText().toString());
                String text = editor.getText().toString();
                String[] lines = text.split("\n", -1);
                
                if (line > 0 && line <= lines.length) {
                    int pos = 0;
                    for (int i = 0; i < line - 1; i++) {
                        pos += lines[i].length() + 1;
                    }
                    editor.setSelection(pos);
                    editor.requestFocus();
                    Toast.makeText(this, "Line " + line, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Invalid line number", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void loadFiles() {
        loadFiles(new File(projectPath), fileList, 0);
    }

    private void loadFiles(File dir, LinearLayout container, int depth) {
        container.removeAllViews();
        if (!dir.exists()) dir.mkdirs();
        
        SharedPreferences themePrefs = getSharedPreferences("GitCodeTheme", MODE_PRIVATE);
        boolean isDark = themePrefs.getBoolean("darkMode", false);
        
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            if (depth == 0) {
                TextView empty = new TextView(this);
                empty.setText("No files yet");
                empty.setPadding(20, 20, 20, 20);
                if (isDark) {
                    empty.setTextColor(0xFFAAAAAA);
                }
                container.addView(empty);
            }
            return;
        }
        
        // Sort: folders first, then files
        java.util.Arrays.sort(files, (f1, f2) -> {
            if (f1.isDirectory() && !f2.isDirectory()) return -1;
            if (!f1.isDirectory() && f2.isDirectory()) return 1;
            return f1.getName().compareToIgnoreCase(f2.getName());
        });
        
        for (File file : files) {
            createFileItem(file, container, depth);
        }
    }

    private void createFileItem(File file, LinearLayout container, int depth) {
        SharedPreferences themePrefs = getSharedPreferences("GitCodeTheme", MODE_PRIVATE);
        boolean isDark = themePrefs.getBoolean("darkMode", false);
        
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.VERTICAL);
        
        LinearLayout rowLayout = new LinearLayout(this);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setPadding(depth * 30 + 5, 5, 5, 5);
        
        if (selectionMode) {
            android.widget.CheckBox cb = new android.widget.CheckBox(this);
            cb.setChecked(selectedFiles.contains(file));
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedFiles.add(file);
                } else {
                    selectedFiles.remove(file);
                }
            });
            rowLayout.addView(cb);
        }
        
        if (file.isDirectory()) {
            TextView arrow = new TextView(this);
            arrow.setText("▶ ");
            arrow.setTextSize(12);
            arrow.setPadding(0, 10, 5, 0);
            arrow.setTag("collapsed");
            if (isDark) {
                arrow.setTextColor(0xFFE0E0E0);
            }
            rowLayout.addView(arrow);
            
            TextView tv = new TextView(this);
            tv.setText("📁 " + file.getName());
            tv.setPadding(5, 10, 15, 10);
            tv.setTextSize(16);
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            if (isDark) {
                tv.setTextColor(0xFFE0E0E0);
            }
            
            tv.setOnLongClickListener(v -> {
                if (!selectionMode) {
                    enterSelectionMode(file);
                }
                return true;
            });
            
            rowLayout.addView(tv);
            
            LinearLayout subContainer = new LinearLayout(this);
            subContainer.setOrientation(LinearLayout.VERTICAL);
            subContainer.setVisibility(View.GONE);
            
            View.OnClickListener toggleFolder = v -> {
                if (selectionMode) return;
                if (arrow.getTag().equals("collapsed")) {
                    arrow.setText("▼ ");
                    arrow.setTag("expanded");
                    subContainer.setVisibility(View.VISIBLE);
                    if (subContainer.getChildCount() == 0) {
                        loadFiles(file, subContainer, depth + 1);
                    }
                } else {
                    arrow.setText("▶ ");
                    arrow.setTag("collapsed");
                    subContainer.setVisibility(View.GONE);
                }
            };
            
            arrow.setOnClickListener(toggleFolder);
            tv.setOnClickListener(toggleFolder);
            rowLayout.setOnClickListener(toggleFolder);
            
            if (!selectionMode) {
                TextView btnMenu = new TextView(this);
                btnMenu.setText("⋮");
                btnMenu.setTextSize(22);
                btnMenu.setPadding(20, 0, 15, 0);
                btnMenu.setOnClickListener(v -> showFolderMenu(file));
                rowLayout.addView(btnMenu);
            }
            
            itemLayout.addView(rowLayout);
            itemLayout.addView(subContainer);
        } else {
            TextView tv = new TextView(this);
            tv.setText("📄 " + file.getName());
            tv.setPadding(15, 10, 15, 10);
            tv.setTextSize(16);
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            if (isDark) {
                tv.setTextColor(0xFFE0E0E0);
            }
            
            tv.setOnClickListener(v -> {
                if (!selectionMode) {
                    openFile(file);
                }
            });
            
            tv.setOnLongClickListener(v -> {
                if (!selectionMode) {
                    enterSelectionMode(file);
                }
                return true;
            });
            
            rowLayout.addView(tv);
            
            if (!selectionMode) {
                TextView btnMenu = new TextView(this);
                btnMenu.setText("⋮");
                btnMenu.setTextSize(22);
                btnMenu.setPadding(20, 0, 15, 0);
                btnMenu.setOnClickListener(v -> showFileMenu(file));
                rowLayout.addView(btnMenu);
            }
            
            itemLayout.addView(rowLayout);
        }
        
        container.addView(itemLayout);
    }

    private void enterSelectionMode(File file) {
        selectionMode = true;
        selectedFiles.clear();
        selectedFiles.add(file);
        updateActionsButton();
        loadFiles();
    }

    private void toggleSelectionMode() {
        selectionMode = !selectionMode;
        selectedFiles.clear();
        updateActionsButton();
        loadFiles();
    }

    private void updateActionsButton() {
        View drawer = findViewById(android.R.id.content).getRootView().findViewWithTag("actionsBtn");
        if (drawer != null) {
            drawer.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
        }
    }

    private void showSelectionMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selected: " + selectedFiles.size());
        
        String[] options = new String[]{"Move", "Delete", "Done"};
        
        builder.setItems(options, (d, which) -> {
            switch (which) {
                case 0: 
                    moveSelectedFiles(); 
                    break;
                case 1: 
                    deleteSelectedFiles(); 
                    break;
                case 2: 
                    exitSelectionMode();
                    break;
            }
        });
        builder.show();
    }

    private void exitSelectionMode() {
        selectionMode = false;
        selectedFiles.clear();
        updateActionsButton();
        loadFiles();
    }

    private void moveSelectedFiles() {
        if (selectedFiles.isEmpty()) {
            Toast.makeText(this, "No files selected", Toast.LENGTH_SHORT).show();
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Move to folder");
        
        java.util.List<File> folders = new java.util.ArrayList<>();
        folders.add(new File(projectPath));
        collectFolders(new File(projectPath), folders);
        
        String[] folderNames = new String[folders.size()];
        for (int i = 0; i < folders.size(); i++) {
            File f = folders.get(i);
            if (f.getPath().equals(projectPath)) {
                folderNames[i] = "/ (Root)";
            } else {
                folderNames[i] = f.getPath().replace(projectPath + "/", "");
            }
        }
        
        builder.setItems(folderNames, (d, which) -> {
            File targetFolder = folders.get(which);
            int moved = 0;
            
            for (File file : selectedFiles) {
                if (file.getParentFile().equals(targetFolder)) {
                    continue;
                }
                File newFile = new File(targetFolder, file.getName());
                if (file.renameTo(newFile)) {
                    moved++;
                }
            }
            
            Toast.makeText(this, "Moved " + moved + " items", Toast.LENGTH_SHORT).show();
            exitSelectionMode();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void collectFolders(File dir, java.util.List<File> folders) {
        File[] files = dir.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                folders.add(file);
                collectFolders(file, folders);
            }
        }
    }

    private void deleteSelectedFiles() {
        if (selectedFiles.isEmpty()) {
            Toast.makeText(this, "No files selected", Toast.LENGTH_SHORT).show();
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete " + selectedFiles.size() + " items?");
        builder.setMessage("This action cannot be undone.");
        builder.setPositiveButton("Delete", (d, w) -> {
            int deleted = 0;
            for (File file : selectedFiles) {
                if (deleteRecursive(file)) {
                    deleted++;
                    if (currentFile != null && currentFile.equals(file)) {
                        currentFile = null;
                        editor.setText("");
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setSubtitle("");
                        }
                    }
                }
            }
            Toast.makeText(this, "Deleted " + deleted + " items", Toast.LENGTH_SHORT).show();
            exitSelectionMode();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showFolderMenu(File folder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(folder.getName());
        
        String[] options = new String[]{"New File Here", "New Folder Here", "Move", "Rename", "Delete"};
        
        builder.setItems(options, (d, which) -> {
            switch (which) {
                case 0: createNewFileInFolder(folder); break;
                case 1: createNewFolderInFolder(folder); break;
                case 2: moveSingleFile(folder); break;
                case 3: renameFile(folder); break;
                case 4: deleteFile(folder); break;
            }
        });
        builder.show();
    }

    private void createNewFileInFolder(File folder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New File in " + folder.getName());
        EditText input = new EditText(this);
        input.setHint("filename.ext");
        input.setPadding(50, 20, 50, 20);
        builder.setView(input);
        builder.setPositiveButton("Create", (d, w) -> {
            String name = input.getText().toString().trim();
            if (name.isEmpty()) return;
            
            File file = new File(folder, name);
            try {
                file.createNewFile();
                loadFiles();
                Toast.makeText(this, "File created", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void createNewFolderInFolder(File parent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Folder in " + parent.getName());
        EditText input = new EditText(this);
        input.setHint("folder name");
        input.setPadding(50, 20, 50, 20);
        builder.setView(input);
        builder.setPositiveButton("Create", (d, w) -> {
            String name = input.getText().toString().trim();
            if (name.isEmpty()) return;
            
            File folder = new File(parent, name);
            if (folder.exists()) {
                Toast.makeText(this, "Cannot create - name already in use", Toast.LENGTH_SHORT).show();
                return;
            }
            
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

    private void expandFolder(File folder, LinearLayout container) {
        // Removed - now handled inline with tree view
    }

    private void showFileMenu(File file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(file.getName());
        
        String[] options = new String[]{"Open", "Move", "Rename", "Delete"};
        
        builder.setItems(options, (d, which) -> {
            switch (which) {
                case 0: openFile(file); break;
                case 1: moveSingleFile(file); break;
                case 2: renameFile(file); break;
                case 3: deleteFile(file); break;
            }
        });
        builder.show();
    }

    private void moveSingleFile(File file) {
        selectedFiles.clear();
        selectedFiles.add(file);
        moveSelectedFiles();
    }

    private void renameFile(File file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename");
        EditText input = new EditText(this);
        input.setText(file.getName());
        input.setSelection(file.getName().length());
        input.setPadding(50, 20, 50, 20);
        builder.setView(input);
        builder.setPositiveButton("Rename", (d, w) -> {
            String newName = input.getText().toString().trim();
            if (newName.isEmpty() || newName.equals(file.getName())) return;
            
            File newFile = new File(file.getParent(), newName);
            if (newFile.exists()) {
                Toast.makeText(this, "File already exists", Toast.LENGTH_SHORT).show();
                return;
            }
            
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
            if (folder.exists()) {
                Toast.makeText(this, "Cannot create - name already in use", Toast.LENGTH_SHORT).show();
                return;
            }
            
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
            
            undoStack.clear();
            redoStack.clear();
            
            editor.setText(content);
            applySyntaxHighlighting(file.getName(), content);
            
            if (getSupportActionBar() != null) {
                getSupportActionBar().setSubtitle(file.getName());
            }
            drawerLayout.closeDrawer(Gravity.START);
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateLineNumbers(TextView lineNumbers, String text) {
        int lines = text.isEmpty() ? 1 : text.split("\n", -1).length;
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= lines; i++) {
            sb.append(i);
            if (i < lines) sb.append("\n");
        }
        lineNumbers.setText(sb.toString());
        
        // Match height with editor content
        lineNumbers.post(() -> {
            int editorHeight = editor.getHeight();
            int lineNumberHeight = lineNumbers.getHeight();
            if (editorHeight > lineNumberHeight) {
                lineNumbers.setMinHeight(editorHeight);
            }
        });
    }

    private void addCompactButton(LinearLayout toolbar, String icon, View.OnClickListener listener) {
        android.widget.Button btn = new android.widget.Button(this);
        btn.setText(icon);
        btn.setTextSize(18);
        btn.setPadding(20, 0, 20, 0);
        btn.setOnClickListener(listener);
        btn.setBackgroundColor(0x00000000);
        btn.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
        toolbar.addView(btn);
    }

    private void applySyntaxHighlighting(String fileName, String content) {
        if (content == null || content.isEmpty()) {
            editor.setText("");
            return;
        }
        
        String ext = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            ext = fileName.substring(i + 1).toLowerCase();
        }
        
        try {
            android.text.SpannableString spannable = new android.text.SpannableString(content);
            
            String[] keywords = getKeywordsForExtension(ext);
            int keywordColor = 0xFF0000FF;
            int stringColor = 0xFF008000;
            int commentColor = 0xFF808080;
            
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
            highlightPattern(spannable, content, "#.*", commentColor);
            
            editor.setText(spannable);
        } catch (Exception e) {
            editor.setText(content);
        }
    }

    private void highlightPattern(android.text.Spannable spannable, String content, String regex, int color) {
        try {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
            java.util.regex.Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                spannable.setSpan(new android.text.style.ForegroundColorSpan(color),
                    matcher.start(), matcher.end(),
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        } catch (Exception e) {
            // Skip if pattern fails
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
        input.requestFocus();
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
            if (file.exists()) {
                Toast.makeText(this, "Cannot create - name already in use", Toast.LENGTH_SHORT).show();
                return;
            }
            
            try {
                if (file.createNewFile()) {
                    currentFile = file;
                    editor.setText("");
                    undoStack.clear();
                    redoStack.clear();
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setSubtitle(name);
                    }
                    loadFiles();
                    Toast.makeText(this, "Created", Toast.LENGTH_SHORT).show();
                }
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
        
        autoSaveFile();
        Toast.makeText(this, "Saved ✓", Toast.LENGTH_SHORT).show();
    }

    private void autoSaveFile() {
        if (currentFile == null) return;
        
        try {
            FileOutputStream fos = new FileOutputStream(currentFile);
            fos.write(editor.getText().toString().getBytes());
            fos.close();
        } catch (Exception e) {
            // Silent fail for auto-save
        }
    }

    private void commitAndPushAll() {
        if (currentFile != null) {
            autoSaveFile();
        }
        
        String username = prefs.getString("username", "");
        String token = prefs.getString("token", "");
        
        if (username.isEmpty() || token.isEmpty()) {
            Toast.makeText(this, "Configure GitHub settings first", Toast.LENGTH_LONG).show();
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Commit & Push");
        EditText input = new EditText(this);
        input.setHint("Commit message");
        input.setText("Update project");
        input.setSelection(input.getText().length());
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
        Toast.makeText(this, "Pushing changes...", Toast.LENGTH_SHORT).show();
        executor.execute(() -> {
            GitHubAPI api = new GitHubAPI(username, token, repo);
            File dir = new File(projectPath);
            
            // Get last push data
            SharedPreferences pushPrefs = getSharedPreferences("GitCodePush_" + projectName, MODE_PRIVATE);
            
            // Track current files
            java.util.Set<String> currentFiles = new java.util.HashSet<>();
            java.util.List<String> results = new java.util.ArrayList<>();
            
            // Push modified/added files
            int pushed = pushModifiedFiles(api, dir, "", message, results, pushPrefs, currentFiles);
            
            // Detect and delete removed files
            java.util.Set<String> previousFiles = pushPrefs.getStringSet("pushedFiles", new java.util.HashSet<>());
            int deleted = 0;
            for (String oldFile : previousFiles) {
                // Skip folder markers (they're just for tracking)
                if (oldFile.endsWith("/")) continue;
                
                if (!currentFiles.contains(oldFile)) {
                    String result = api.deleteFile(oldFile, message);
                    if (result.contains("Success")) {
                        // Count .gitkeep deletion as folder deletion
                        if (oldFile.endsWith("/.gitkeep")) {
                            deleted++;
                        } else {
                            deleted++;
                        }
                        pushPrefs.edit().remove(oldFile).apply();
                    }
                }
            }
            
            // Update tracking
            SharedPreferences.Editor editor = pushPrefs.edit();
            editor.putLong("lastPushTime", System.currentTimeMillis());
            editor.putStringSet("pushedFiles", currentFiles);
            editor.apply();
            
            int finalPushed = pushed;
            int finalDeleted = deleted;
            runOnUiThread(() -> {
                if (finalPushed > 0 || finalDeleted > 0) {
                    String msg = "";
                    if (finalPushed > 0) msg += finalPushed + " pushed";
                    if (finalDeleted > 0) msg += (msg.isEmpty() ? "" : ", ") + finalDeleted + " deleted";
                    Toast.makeText(this, "✓ " + msg, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "No changes to sync", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private int pushModifiedFiles(GitHubAPI api, File dir, String relativePath, String message, 
                                   java.util.List<String> results, SharedPreferences pushPrefs,
                                   java.util.Set<String> currentFiles) {
        int success = 0;
        File[] files = dir.listFiles();
        
        if (files == null || files.length == 0) {
            return 0;
        }
        
        for (File file : files) {
            String filePath = relativePath.isEmpty() ? file.getName() : relativePath + "/" + file.getName();
            
            if (file.isDirectory()) {
                // Mark as folder with trailing slash
                currentFiles.add(filePath + "/");
                
                File[] subFiles = file.listFiles();
                if (subFiles == null || subFiles.length == 0) {
                    String gitkeepPath = filePath + "/.gitkeep";
                    currentFiles.add(gitkeepPath);
                    long gitkeepTime = pushPrefs.getLong(gitkeepPath, 0);
                    if (gitkeepTime == 0) {
                        String result = api.commitAndPush(gitkeepPath, "", message);
                        if (result.contains("Success")) {
                            pushPrefs.edit().putLong(gitkeepPath, System.currentTimeMillis()).apply();
                            success++; // Count folder creation
                        }
                    }
                } else {
                    success += pushModifiedFiles(api, file, filePath, message, results, pushPrefs, currentFiles);
                }
            } else {
                long fileModified = file.lastModified();
                currentFiles.add(filePath);
                
                long lastPushed = pushPrefs.getLong(filePath, 0);
                
                if (fileModified > lastPushed) {
                    try {
                        FileInputStream fis = new FileInputStream(file);
                        byte[] data = new byte[(int) file.length()];
                        fis.read(data);
                        fis.close();
                        
                        String content = new String(data);
                        String result = api.commitAndPush(filePath, content, message);
                        if (result.contains("Success")) {
                            pushPrefs.edit().putLong(filePath, fileModified).apply();
                            success++;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
        return success;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        autoSaveHandler.removeCallbacks(autoSaveRunnable);
        executor.shutdown();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Auto-save when leaving the activity
        if (currentFile != null) {
            autoSaveFile();
        }
    }
}
