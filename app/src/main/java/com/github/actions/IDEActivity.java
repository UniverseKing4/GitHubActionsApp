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
    private TextView lineNumbers;
    private ScrollView editorScroll;
    private ScrollView lineNumberScroll;
    private String projectName, projectPath;
    private File currentFile;
    private SharedPreferences prefs;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean selectionMode = false;
    private java.util.Set<File> selectedFiles = new java.util.HashSet<>();
    private android.os.Handler autoSaveHandler = new android.os.Handler();
    private Runnable autoSaveRunnable;
    private android.os.Handler syntaxHandler = new android.os.Handler();
    private Runnable syntaxRunnable;
    private String lastHighlightedText = "";
    private java.util.Stack<String> undoStack = new java.util.Stack<>();
    private java.util.Stack<String> redoStack = new java.util.Stack<>();
    private boolean isUndoRedo = false;
    private long lastEditTime = 0;
    private static final long UNDO_DELAY = 1000; // 1 second
    private boolean wordWrapEnabled = true;
    private android.text.style.BackgroundColorSpan bracketHighlight;
    private java.util.List<File> openTabs = new java.util.ArrayList<>();
    private LinearLayout tabBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        projectName = getIntent().getStringExtra("projectName");
        projectPath = getIntent().getStringExtra("projectPath");
        prefs = getSharedPreferences("GitHubCreds", MODE_PRIVATE);
        
        // Apply dark mode
        SharedPreferences themePrefs = getSharedPreferences("GitCodeTheme", MODE_PRIVATE);
        boolean isDark = themePrefs.getBoolean("darkMode", true);
        
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
        
        // Tab bar with horizontal scrolling
        android.widget.HorizontalScrollView tabScroll = new android.widget.HorizontalScrollView(this);
        tabScroll.setHorizontalScrollBarEnabled(false);
        tabScroll.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
        
        tabBar = new LinearLayout(this);
        tabBar.setOrientation(LinearLayout.HORIZONTAL);
        tabBar.setBackgroundColor(isDark ? 0xFF2D2D2D : 0xFFE0E0E0);
        tabBar.setPadding(5, 5, 5, 5);
        tabScroll.addView(tabBar);
        mainLayout.addView(tabScroll);
        
        // Editor container
        LinearLayout editorContainer = new LinearLayout(this);
        editorContainer.setOrientation(LinearLayout.HORIZONTAL);
        editorContainer.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT));
        if (isDark) {
            editorContainer.setBackgroundColor(0xFF1E1E1E);
        }
        
        // Line numbers - start with minimal width
        lineNumberScroll = new ScrollView(this);
        lineNumberScroll.setVerticalScrollBarEnabled(false);
        LinearLayout.LayoutParams lineNumParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.MATCH_PARENT);
        lineNumberScroll.setLayoutParams(lineNumParams);
        
        // Load saved font size
        SharedPreferences settingsPrefs = getSharedPreferences("GitCodeSettings", MODE_PRIVATE);
        int fontSize = settingsPrefs.getInt("fontSize", 14);
        
        lineNumbers = new TextView(this);
        lineNumbers.setTypeface(android.graphics.Typeface.MONOSPACE);
        lineNumbers.setTextSize(fontSize);
        
        lineNumbers.setGravity(Gravity.TOP | Gravity.END);
        lineNumbers.setPadding(8, 20, 2, 20);
        lineNumbers.setBackgroundColor(isDark ? 0xFF2D2D2D : 0xFFF5F5F5);
        lineNumbers.setTextColor(isDark ? 0xFF666666 : 0xFF999999);
        lineNumbers.setLineSpacing(0, 1.0f);
        lineNumbers.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
        lineNumberScroll.addView(lineNumbers);
        editorContainer.addView(lineNumberScroll);
        
        editorScroll = new ScrollView(this);
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
            LinearLayout.LayoutParams.WRAP_CONTENT));
        editor.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        editor.setTypeface(android.graphics.Typeface.MONOSPACE);
        editor.setTextSize(fontSize);
        
        editor.setLineSpacing(0, 1.0f);
        editor.setGravity(Gravity.TOP | Gravity.START);
        editor.setPadding(10, 20, 15, 20);
        editor.setHorizontallyScrolling(false);
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
        
        // Syntax highlighting setup
        syntaxRunnable = () -> {
            if (currentFile != null) {
                String currentText = editor.getText().toString();
                if (!currentText.equals(lastHighlightedText)) {
                    if (isLargeFile) {
                        applySyntaxHighlightingForLargeFile();
                    } else {
                        applySyntaxHighlighting(currentFile.getName(), currentText);
                    }
                    lastHighlightedText = currentText;
                }
            }
        };
        
        // Auto-indent and auto-brackets
        editor.addTextChangedListener(new android.text.TextWatcher() {
            private String before = "";
            private int cursorPos = 0;
            private boolean isProcessing = false;
            private long lastUpdateTime = 0;
            
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                before = s.toString();
                cursorPos = start;
            }
            
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            public void afterTextChanged(android.text.Editable s) {
                if (isProcessing) return;
                isProcessing = true;
                
                String text = s.toString();
                long currentTime = System.currentTimeMillis();
                
                // For large files, track undo/redo on full content
                if (isLargeFile) {
                    // Track for undo/redo with time-based grouping
                    if (!isUndoRedo && currentTime - lastEditTime > UNDO_DELAY) {
                        updateFullContentFromChunk();
                        if (!undoStack.isEmpty() && !undoStack.peek().equals(fullFileContent)) {
                            undoStack.push(fullFileContent);
                        } else if (undoStack.isEmpty()) {
                            undoStack.push(fullFileContent);
                        }
                        if (undoStack.size() > 50) undoStack.remove(0);
                        redoStack.clear();
                        lastEditTime = currentTime;
                    }
                    
                    // Trigger syntax highlighting after 2 seconds
                    syntaxHandler.removeCallbacks(syntaxRunnable);
                    syntaxHandler.postDelayed(syntaxRunnable, 2000);
                    
                    // Auto-save
                    autoSaveHandler.removeCallbacks(autoSaveRunnable);
                    autoSaveHandler.postDelayed(autoSaveRunnable, 2000);
                    isProcessing = false;
                    return;
                }
                
                // Update line numbers only every 100ms to reduce lag
                if (currentTime - lastUpdateTime > 100) {
                    updateLineNumbers(lineNumbers, text);
                    lastUpdateTime = currentTime;
                }
                
                // Trigger syntax highlighting after 2 seconds of inactivity
                syntaxHandler.removeCallbacks(syntaxRunnable);
                syntaxHandler.postDelayed(syntaxRunnable, 2000);
                
                // Always trigger auto-save
                autoSaveHandler.removeCallbacks(autoSaveRunnable);
                autoSaveHandler.postDelayed(autoSaveRunnable, 2000);
                
                // Track for undo/redo with time-based grouping
                if (!isUndoRedo && !text.equals(before)) {
                    long undoTime = System.currentTimeMillis();
                    // Only save to undo stack if enough time has passed or significant change
                    if (undoTime - lastEditTime > UNDO_DELAY) {
                        if (!undoStack.isEmpty() && !undoStack.peek().equals(before)) {
                            undoStack.push(before);
                        } else if (undoStack.isEmpty()) {
                            undoStack.push(before);
                        }
                        if (undoStack.size() > 50) undoStack.remove(0);
                        redoStack.clear();
                    }
                    lastEditTime = undoTime;
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
        
        // Bracket matching
        editor.setOnClickListener(v -> highlightMatchingBracket());
        
        editorScroll.addView(editor);
        editorContainer.addView(editorScroll);
        
        // Sync scroll on touch
        editorScroll.getViewTreeObserver().addOnScrollChangedListener(() -> {
            int scrollY = editorScroll.getScrollY();
            lineNumberScroll.scrollTo(0, scrollY);
        });
        
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
        btnContainer.setPadding(5, 0, 5, 10);
        btnContainer.setId(View.generateViewId());
        
        android.widget.Button btnNewFile = new android.widget.Button(this);
        btnNewFile.setText("+ File");
        btnNewFile.setTextSize(12);
        btnNewFile.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        btnNewFile.setOnClickListener(v -> createNewFile());
        btnContainer.addView(btnNewFile);
        
        android.widget.Button btnNewFolder = new android.widget.Button(this);
        btnNewFolder.setText("+ Folder");
        btnNewFolder.setTextSize(12);
        btnNewFolder.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        btnNewFolder.setOnClickListener(v -> createNewFolder());
        btnContainer.addView(btnNewFolder);
        
        android.widget.Button btnSelect = new android.widget.Button(this);
        btnSelect.setText("Select");
        btnSelect.setTextSize(12);
        btnSelect.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        btnSelect.setTag("selectBtn");
        btnSelect.setOnClickListener(v -> {
            toggleSelectionMode();
            updateFileMenuButtons();
        });
        btnContainer.addView(btnSelect);
        
        android.widget.Button btnActions = new android.widget.Button(this);
        btnActions.setText("Actions");
        btnActions.setTextSize(12);
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
            
            // Create title with project name
            TextView titleView = new TextView(this);
            titleView.setText(projectName);
            titleView.setTextColor(isDark ? 0xFFFFFFFF : 0xFF000000);
            titleView.setTextSize(14);
            titleView.setPadding(10, 0, 10, 0);
            titleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
            titleParams.setMargins(0, 0, 20, 0);
            titleView.setLayoutParams(titleParams);
            titleView.setMaxWidth((int)(150 * getResources().getDisplayMetrics().density));
            titleView.setSingleLine(true);
            titleView.setEllipsize(android.text.TextUtils.TruncateAt.END);
            
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(android.R.drawable.ic_menu_sort_by_size);
            
            // Add custom view with title and toolbar buttons
            LinearLayout toolbarContainer = new LinearLayout(this);
            toolbarContainer.setOrientation(LinearLayout.HORIZONTAL);
            toolbarContainer.setGravity(Gravity.CENTER_VERTICAL);
            toolbarContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
            
            toolbarContainer.addView(titleView);
            
            LinearLayout toolbarView = new LinearLayout(this);
            toolbarView.setOrientation(LinearLayout.HORIZONTAL);
            toolbarView.setGravity(Gravity.END);
            toolbarView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1));
            
            addCompactButton(toolbarView, "💾", v -> saveCurrentFile());
            addCompactButton(toolbarView, "↶", v -> undo());
            addCompactButton(toolbarView, "↷", v -> redo());
            addCompactButton(toolbarView, "🚀", v -> commitAndPushAll());
            
            toolbarContainer.addView(toolbarView);
            
            getSupportActionBar().setCustomView(toolbarContainer);
            getSupportActionBar().setDisplayShowCustomEnabled(true);
        }
        
        loadFiles();
        
        // Open last file or show welcome message
        SharedPreferences filePrefs = getSharedPreferences("GitCodeFiles", MODE_PRIVATE);
        String lastFile = filePrefs.getString("lastFile_" + projectName, "");
        
        if (!lastFile.isEmpty()) {
            File file = new File(lastFile);
            if (file.exists()) {
                openFile(file);
            } else {
                showWelcomeMessage();
            }
        } else {
            showWelcomeMessage();
        }
    }
    
    @Override
    public void onBackPressed() {
        // Save current file before going back
        if (currentFile != null) {
            try {
                // For large files, update full content from current chunk first
                if (isLargeFile) {
                    updateFullContentFromChunk();
                    java.io.FileWriter writer = new java.io.FileWriter(currentFile);
                    writer.write(fullFileContent);
                    writer.close();
                } else {
                    // For small files, save editor content directly
                    String content = editor.getText().toString();
                    java.io.FileWriter writer = new java.io.FileWriter(currentFile);
                    writer.write(content);
                    writer.close();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        super.onBackPressed();
    }

    private void showWelcomeMessage() {
        File dir = new File(projectPath);
        File[] files = dir.listFiles();
        
        if (files == null || files.length == 0) {
            editor.setText("No files in project.\n\nTap the menu icon (☰) to create a new file or folder.");
            editor.setEnabled(false);
        } else {
            // Find first file and open it
            for (File file : files) {
                if (file.isFile()) {
                    openFile(file);
                    return;
                }
            }
            // If only folders exist
            editor.setText("No files in project.\n\nTap the menu icon (☰) to create a new file.");
            editor.setEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Find");
        menu.add(0, 2, 0, "Replace");
        menu.add(0, 3, 0, "Go to Line");
        menu.add(0, 6, 0, "Delete Line");
        menu.add(0, 5, 0, "Duplicate Line");
        menu.add(0, 4, 0, "Select All");
        menu.add(0, 7, 0, "Word Wrap: ON");
        menu.add(0, 8, 0, "⬇ Pull from GitHub");
        menu.add(0, 9, 0, "📊 Project Statistics");
        menu.add(0, 10, 0, "⚙ Settings");
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
            case 8:
                pullFromGitHub();
                return true;
            case 9:
                showProjectStats();
                return true;
            case 10:
                showSettings();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private AlertDialog.Builder createThemedDialog() {
        return new AlertDialog.Builder(this);
    }
    
    private AlertDialog showThemedDialog(AlertDialog.Builder builder) {
        SharedPreferences themePrefs = getSharedPreferences("GitCodeTheme", MODE_PRIVATE);
        boolean isDark = themePrefs.getBoolean("darkMode", true);
        
        AlertDialog dialog = builder.create();
        
        // Apply dark theme BEFORE showing to prevent flash
        if (isDark && dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0xFF2D2D2D));
        }
        
        dialog.show();
        
        // Apply text colors after showing
        if (isDark && dialog.getWindow() != null) {
            new android.os.Handler().post(() -> {
                try {
                    android.view.ViewGroup root = (android.view.ViewGroup) dialog.getWindow().getDecorView();
                    applyDarkTheme(root);
                    
                    // Make dialog button text white
                    android.widget.Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    android.widget.Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                    android.widget.Button neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                    
                    if (positiveButton != null) positiveButton.setTextColor(0xFFFFFFFF);
                    if (negativeButton != null) negativeButton.setTextColor(0xFFFFFFFF);
                    if (neutralButton != null) neutralButton.setTextColor(0xFFFFFFFF);
                } catch (Exception e) {
                    // Ignore errors
                }
            });
        }
        
        return dialog;
    }
    
    private void applyDarkTheme(android.view.View view) {
        if (view instanceof EditText) {
            ((EditText) view).setTextColor(0xFFFFFFFF);
            ((EditText) view).setHintTextColor(0xFF888888);
        } else if (view instanceof android.widget.Button) {
            // Keep button text black for visibility
            ((android.widget.Button) view).setTextColor(0xFF000000);
        } else if (view instanceof TextView) {
            ((TextView) view).setTextColor(0xFFFFFFFF);
        }
        
        if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyDarkTheme(group.getChildAt(i));
            }
        }
    }

    private void showSettings() {
        AlertDialog.Builder builder = createThemedDialog();
        builder.setTitle("Editor Settings");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        
        TextView label = new TextView(this);
        label.setText("Font Size:");
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
        
        builder.setView(layout);
        builder.setPositiveButton("Apply", (d, w) -> {
            int size = seekBar.getProgress() + 10;
            editor.setTextSize(size);
            lineNumbers.setTextSize(size);
            settingsPrefs.edit().putInt("fontSize", size).apply();
            Toast.makeText(this, "Font size: " + size + "sp", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", null);
        showThemedDialog(builder);
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
            if (isLargeFile) {
                // For large files, update full content first
                updateFullContentFromChunk();
                redoStack.push(fullFileContent);
                fullFileContent = undoStack.pop();
                isUndoRedo = true;
                // Reload current chunk
                if (useLineBasedChunking) {
                    loadChunkWithButtons(currentChunkLine);
                } else {
                    loadChunkWithButtons(currentChunkStart);
                }
                isUndoRedo = false;
                Toast.makeText(this, "Undo", Toast.LENGTH_SHORT).show();
            } else {
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
                Toast.makeText(this, "Undo", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Nothing to undo", Toast.LENGTH_SHORT).show();
        }
    }

    private void redo() {
        if (!redoStack.isEmpty()) {
            if (isLargeFile) {
                // For large files, update full content first
                updateFullContentFromChunk();
                undoStack.push(fullFileContent);
                fullFileContent = redoStack.pop();
                isUndoRedo = true;
                // Reload current chunk
                if (useLineBasedChunking) {
                    loadChunkWithButtons(currentChunkLine);
                } else {
                    loadChunkWithButtons(currentChunkStart);
                }
                isUndoRedo = false;
                Toast.makeText(this, "Redo", Toast.LENGTH_SHORT).show();
            } else {
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
                Toast.makeText(this, "Redo", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Nothing to redo", Toast.LENGTH_SHORT).show();
        }
    }

    private int currentFindIndex = 0;
    private java.util.List<Integer> findOccurrences = new java.util.ArrayList<>();
    private String lastSearchText = "";
    private String lastReplaceText = "";
    private boolean isLargeFile = false;
    private String fullFileContent = "";
    private int currentChunkStart = 0;
    private int currentChunkLine = 0; // For line-based chunking
    private static final int CHUNK_SIZE = 10000; // Characters per chunk
    private static final int CHUNK_LINES = 500; // Lines per chunk for line-based files
    private boolean useLineBasedChunking = false;
    
    private void showFindDialog() {
        AlertDialog.Builder builder = createThemedDialog();
        builder.setTitle("Find");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        
        EditText input = new EditText(this);
        input.setHint("Search text");
        input.setText(lastSearchText);
        layout.addView(input);
        
        TextView countText = new TextView(this);
        countText.setPadding(0, 10, 0, 10);
        layout.addView(countText);
        
        builder.setView(layout);
        builder.setPositiveButton("Find All", (d, w) -> {
            String search = input.getText().toString();
            if (!search.isEmpty()) {
                lastSearchText = search;
                findOccurrences.clear();
                currentFindIndex = 0;
                
                String text = editor.getText().toString();
                int index = 0;
                while ((index = text.indexOf(search, index)) >= 0) {
                    findOccurrences.add(index);
                    index += search.length();
                }
                
                if (!findOccurrences.isEmpty()) {
                    int pos = findOccurrences.get(0);
                    editor.setSelection(pos, pos + search.length());
                    editor.requestFocus();
                    Toast.makeText(this, "Found " + findOccurrences.size() + " occurrences", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Not found", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNeutralButton("Next", (d, w) -> {
            if (!findOccurrences.isEmpty() && !lastSearchText.isEmpty()) {
                currentFindIndex = (currentFindIndex + 1) % findOccurrences.size();
                int pos = findOccurrences.get(currentFindIndex);
                editor.setSelection(pos, pos + lastSearchText.length());
                editor.requestFocus();
                Toast.makeText(this, (currentFindIndex + 1) + " of " + findOccurrences.size(), Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        showThemedDialog(builder);
    }

    private void showReplaceDialog() {
        AlertDialog.Builder builder = createThemedDialog();
        builder.setTitle("Find & Replace");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        
        EditText findInput = new EditText(this);
        findInput.setHint("Find");
        findInput.setText(lastSearchText);
        layout.addView(findInput);
        
        EditText replaceInput = new EditText(this);
        replaceInput.setHint("Replace with");
        replaceInput.setText(lastReplaceText);
        layout.addView(replaceInput);
        
        builder.setView(layout);
        builder.setPositiveButton("Replace All", (d, w) -> {
            String find = findInput.getText().toString();
            String replace = replaceInput.getText().toString();
            lastSearchText = find;
            lastReplaceText = replace;
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
                    int cursorPos = editor.getSelectionStart();
                    editor.setText(newText);
                    if (cursorPos <= newText.length()) {
                        editor.setSelection(cursorPos);
                    }
                    Toast.makeText(this, "Replaced " + count + " occurrences", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "No occurrences found", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNeutralButton("Replace Next", (d, w) -> {
            String find = findInput.getText().toString();
            String replace = replaceInput.getText().toString();
            lastSearchText = find;
            lastReplaceText = replace;
            if (!find.isEmpty()) {
                String text = editor.getText().toString();
                int start = editor.getSelectionStart();
                int index = text.indexOf(find, start);
                if (index < 0) {
                    index = text.indexOf(find);
                }
                if (index >= 0) {
                    String newText = text.substring(0, index) + replace + text.substring(index + find.length());
                    editor.setText(newText);
                    editor.setSelection(index, index + replace.length());
                    Toast.makeText(this, "Replaced 1 occurrence", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "No more occurrences", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        showThemedDialog(builder);
    }

    private void showGoToLineDialog() {
        AlertDialog.Builder builder = createThemedDialog();
        
        if (isLargeFile) {
            builder.setTitle("Go to Line or Part");
            android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
            layout.setOrientation(android.widget.LinearLayout.VERTICAL);
            layout.setPadding(50, 20, 50, 20);
            
            TextView lineLabel = new TextView(this);
            lineLabel.setText("Line number:");
            lineLabel.setPadding(0, 0, 0, 5);
            layout.addView(lineLabel);
            
            EditText lineInput = new EditText(this);
            lineInput.setHint("Enter line number");
            lineInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            lineInput.setSingleLine(true);
            layout.addView(lineInput);
            
            TextView partLabel = new TextView(this);
            partLabel.setText("Part number:");
            partLabel.setPadding(0, 20, 0, 5);
            layout.addView(partLabel);
            
            EditText partInput = new EditText(this);
            partInput.setHint("Enter part number");
            partInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            partInput.setSingleLine(true);
            layout.addView(partInput);
            
            builder.setView(layout);
            builder.setPositiveButton("Go", (d, w) -> {
                String lineStr = lineInput.getText().toString().trim();
                String partStr = partInput.getText().toString().trim();
                
                if (!lineStr.isEmpty()) {
                    // Go to line in full file
                    try {
                        int line = Integer.parseInt(lineStr);
                        String[] allLines = fullFileContent.split("\n", -1);
                        
                        if (line > 0 && line <= allLines.length) {
                            // Save current chunk before loading new one
                            updateFullContentFromChunk();
                            
                            // Load the chunk containing this line
                            if (useLineBasedChunking) {
                                // For line-based: calculate which chunk contains this line
                                int targetChunkLine = ((line - 1) / CHUNK_LINES) * CHUNK_LINES;
                                loadChunkWithButtons(targetChunkLine);
                            } else {
                                // For character-based: calculate character position
                                int charPos = 0;
                                for (int i = 0; i < line - 1; i++) {
                                    charPos += allLines[i].length() + 1;
                                }
                                loadChunkWithButtons(charPos);
                            }
                            
                            // Now find the exact cursor position in the displayed text
                            String displayedText = editor.getText().toString();
                            
                            // Find where the actual code starts (after prev button if present)
                            int codeStartInDisplay = 0;
                            if (displayedText.startsWith("▲▲▲")) {
                                int endOfButton = displayedText.indexOf("\n\n");
                                if (endOfButton > 0) {
                                    codeStartInDisplay = endOfButton + 2;
                                }
                            }
                            
                            // Calculate which line in the full file corresponds to first line of current chunk
                            int firstLineOfChunk = fullFileContent.substring(0, currentChunkStart).split("\n", -1).length;
                            if (currentChunkStart == 0) firstLineOfChunk = 1;
                            
                            // Calculate how many lines into the displayed code we need to go
                            int linesIntoChunk = line - firstLineOfChunk;
                            
                            // Find cursor position by counting lines in the displayed code (not including buttons)
                            int cursorPos = codeStartInDisplay;
                            String codeContent = displayedText.substring(codeStartInDisplay);
                            
                            // Remove next button if present
                            if (codeContent.endsWith("▼▼▼")) {
                                int buttonStart = codeContent.lastIndexOf("\n\n▼▼▼");
                                if (buttonStart > 0) {
                                    codeContent = codeContent.substring(0, buttonStart);
                                }
                            }
                            
                            // Now count lines in the actual code
                            String[] codeLines = codeContent.split("\n", -1);
                            for (int i = 0; i < linesIntoChunk && i < codeLines.length; i++) {
                                cursorPos += codeLines[i].length() + 1;
                            }
                            
                            // Set cursor to line start
                            if (cursorPos <= displayedText.length()) {
                                editor.setSelection(Math.min(cursorPos, displayedText.length()));
                                editor.requestFocus();
                            }
                            
                            Toast.makeText(this, "Line " + line, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Invalid line number (1-" + allLines.length + ")", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Invalid line number", Toast.LENGTH_SHORT).show();
                    }
                } else if (!partStr.isEmpty()) {
                    // Go to part
                    try {
                        int part = Integer.parseInt(partStr);
                        int totalParts;
                        
                        if (useLineBasedChunking) {
                            String[] allLines = fullFileContent.split("\n", -1);
                            totalParts = (allLines.length + CHUNK_LINES - 1) / CHUNK_LINES;
                            
                            if (part > 0 && part <= totalParts) {
                                updateFullContentFromChunk();
                                int startLine = (part - 1) * CHUNK_LINES;
                                loadChunkWithButtons(startLine);
                                Toast.makeText(this, "Part " + part + "/" + totalParts, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Invalid part number (1-" + totalParts + ")", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            totalParts = (fullFileContent.length() + CHUNK_SIZE - 1) / CHUNK_SIZE;
                            
                            if (part > 0 && part <= totalParts) {
                                updateFullContentFromChunk();
                                loadChunkWithButtons((part - 1) * CHUNK_SIZE);
                                Toast.makeText(this, "Part " + part + "/" + totalParts, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Invalid part number (1-" + totalParts + ")", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Invalid part number", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Enter line or part number", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Cancel", null);
            showThemedDialog(builder);
        } else {
            // Normal go to line for small files
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
            showThemedDialog(builder);
        }
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
                updateFileMenuButtons();
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
        updateFileMenuButtons();
    }

    private void updateFileMenuButtons() {
        View selectBtn = findViewById(android.R.id.content).getRootView().findViewWithTag("selectBtn");
        View actionsBtn = findViewById(android.R.id.content).getRootView().findViewWithTag("actionsBtn");
        
        if (selectionMode && !selectedFiles.isEmpty()) {
            if (selectBtn != null) selectBtn.setVisibility(View.GONE);
            if (actionsBtn != null) actionsBtn.setVisibility(View.VISIBLE);
        } else {
            if (selectBtn != null) selectBtn.setVisibility(View.VISIBLE);
            if (actionsBtn != null) actionsBtn.setVisibility(View.GONE);
        }
    }

    private void showSelectionMenu() {
        AlertDialog.Builder builder = createThemedDialog();
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
        showThemedDialog(builder);
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
        
        AlertDialog.Builder builder = createThemedDialog();
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
        showThemedDialog(builder);
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
        
        AlertDialog.Builder builder = createThemedDialog();
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
        showThemedDialog(builder);
    }

    private void showFolderMenu(File folder) {
        AlertDialog.Builder builder = createThemedDialog();
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
        showThemedDialog(builder);
    }

    private void createNewFileInFolder(File folder) {
        AlertDialog.Builder builder = createThemedDialog();
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
        showThemedDialog(builder);
    }

    private void createNewFolderInFolder(File parent) {
        AlertDialog.Builder builder = createThemedDialog();
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
        showThemedDialog(builder);
    }

    private void expandFolder(File folder, LinearLayout container) {
        // Removed - now handled inline with tree view
    }

    private void showFileMenu(File file) {
        AlertDialog.Builder builder = createThemedDialog();
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
        showThemedDialog(builder);
    }

    private void moveSingleFile(File file) {
        selectedFiles.clear();
        selectedFiles.add(file);
        moveSelectedFiles();
    }

    private void renameFile(File file) {
        AlertDialog.Builder builder = createThemedDialog();
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
        showThemedDialog(builder);
    }

    private void deleteFile(File file) {
        AlertDialog.Builder builder = createThemedDialog();
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
        showThemedDialog(builder);
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
        AlertDialog.Builder builder = createThemedDialog();
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
        showThemedDialog(builder);
    }

    private void openFile(File file) {
        try {
            // Auto-save current file before switching
            if (currentFile != null && !currentFile.equals(file)) {
                autoSaveFile();
            }
            
            long fileSize = file.length();
            
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) fileSize];
            fis.read(data);
            fis.close();
            
            currentFile = file;
            String content = new String(data);
            
            // Check if large file: >10KB OR 1000+ lines
            int lineCount = content.split("\n", -1).length;
            boolean largeBySize = fileSize > 10000;
            boolean largeByLines = lineCount >= 1000;
            isLargeFile = largeBySize || largeByLines;
            useLineBasedChunking = largeByLines && !largeBySize; // Use line-based if triggered by line count
            
            undoStack.clear();
            redoStack.clear();
            
            if (isLargeFile) {
                // Store full content
                fullFileContent = content;
                currentChunkStart = 0;
                
                // Load first chunk
                loadChunkWithButtons(0);
                
                Toast.makeText(this, "Large file - use Load buttons to navigate", Toast.LENGTH_SHORT).show();
            } else {
                fullFileContent = "";
                useLineBasedChunking = false;
                editor.setText(content);
                editor.setEnabled(true);
                applySyntaxHighlighting(file.getName(), content);
            }
            
            saveFileState(file);
            updateTabsAndUI(file);
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void loadChunkWithButtons(int position) {
        if (fullFileContent.isEmpty()) return;
        
        String chunk;
        int currentPart, totalParts;
        
        if (useLineBasedChunking) {
            // Line-based chunking - position is a line number
            String[] allLines = fullFileContent.split("\n", -1);
            currentChunkLine = position;
            int endLine = Math.min(currentChunkLine + CHUNK_LINES, allLines.length);
            
            currentPart = (currentChunkLine / CHUNK_LINES) + 1;
            totalParts = (allLines.length + CHUNK_LINES - 1) / CHUNK_LINES;
            
            // Calculate character position for startLine
            currentChunkStart = 0;
            for (int i = 0; i < currentChunkLine; i++) {
                currentChunkStart += allLines[i].length() + 1;
            }
            
            // Build chunk from lines
            StringBuilder chunkBuilder = new StringBuilder();
            for (int i = currentChunkLine; i < endLine; i++) {
                chunkBuilder.append(allLines[i]);
                if (i < endLine - 1) chunkBuilder.append("\n");
            }
            chunk = chunkBuilder.toString();
        } else {
            // Character-based chunking - position is a character offset
            currentChunkStart = (position / CHUNK_SIZE) * CHUNK_SIZE;
            int end = Math.min(currentChunkStart + CHUNK_SIZE, fullFileContent.length());
            
            currentPart = (currentChunkStart / CHUNK_SIZE) + 1;
            totalParts = (fullFileContent.length() + CHUNK_SIZE - 1) / CHUNK_SIZE;
            
            chunk = fullFileContent.substring(currentChunkStart, end);
        }
        
        StringBuilder displayText = new StringBuilder();
        
        // Add "Load Previous" button at top if not first chunk
        if (currentPart > 1) {
            displayText.append("▲▲▲ TAP TO LOAD PREVIOUS (").append(currentPart - 1).append("/").append(totalParts).append(") ▲▲▲\n\n");
        }
        
        // Add chunk content
        displayText.append(chunk);
        
        // Add "Load Next" button at bottom if not last chunk
        if (currentPart < totalParts) {
            displayText.append("\n\n▼▼▼ TAP TO LOAD NEXT (").append(currentPart + 1).append("/").append(totalParts).append(") ▼▼▼");
        }
        
        editor.setText(displayText.toString());
        editor.setEnabled(true);
        editor.clearFocus();
        
        // Update line numbers for current chunk
        updateLineNumbersForChunk(chunk, currentChunkStart);
        
        // Setup click listener for load buttons
        setupLoadButtonClicks();
    }
    
    private void updateLineNumbersForChunk(String chunk, int startPos) {
        executor.execute(() -> {
            // Count lines before this chunk in the full file
            String beforeChunk = startPos > 0 ? fullFileContent.substring(0, startPos) : "";
            int startLineNumber = beforeChunk.isEmpty() ? 1 : beforeChunk.split("\n", -1).length;
            
            // Count lines in current chunk
            String[] lines = chunk.split("\n", -1);
            int lineCount = lines.length;
            
            StringBuilder sb = new StringBuilder();
            
            // Add empty lines for "Load Previous" button if present
            // Button format: "▲▲▲ TAP TO LOAD PREVIOUS (X/Y) ▲▲▲\n\n"
            // Line 1: button text (no line number)
            // Line 2: empty (no line number)
            // Line 3: code starts (line number appears here)
            if (startPos > 0) {
                sb.append("\n\n");
            }
            
            // Add line numbers starting from correct position
            for (int i = 0; i < lineCount; i++) {
                sb.append(startLineNumber + i);
                if (i < lineCount - 1) sb.append("\n");
            }
            
            // Add empty lines for "Load Next" button if present
            // Button format: "\n\n▼▼▼ TAP TO LOAD NEXT (X/Y) ▼▼▼"
            // Line 1: empty (no line number)
            // Line 2: empty (no line number)
            // Line 3: button text (no line number)
            boolean hasNextChunk;
            if (useLineBasedChunking) {
                String[] allLines = fullFileContent.split("\n", -1);
                int endLine = currentChunkLine + CHUNK_LINES;
                hasNextChunk = endLine < allLines.length;
            } else {
                hasNextChunk = startPos + CHUNK_SIZE < fullFileContent.length();
            }
            
            if (hasNextChunk) {
                sb.append("\n\n");
            }
            
            String lineNumberText = sb.toString();
            
            runOnUiThread(() -> {
                lineNumbers.setText(lineNumberText);
                lineNumbers.post(() -> {
                    lineNumbers.measure(0, 0);
                    int measuredWidth = lineNumbers.getMeasuredWidth();
                    int perfectWidth = measuredWidth + (int)(4 * getResources().getDisplayMetrics().density);
                    lineNumberScroll.getLayoutParams().width = perfectWidth;
                    lineNumberScroll.requestLayout();
                });
            });
        });
    }
    
    private void setupLoadButtonClicks() {
        // Handle button clicks and block protected lines
        editor.setOnTouchListener((v, event) -> {
            if (!isLargeFile) return false;
            
            int offset = editor.getOffsetForPosition(event.getX(), event.getY());
            String text = editor.getText().toString();
            
            // Find all boundaries
            int prevButtonStart = 0;
            int prevButtonEnd = 0;
            int protectedLine1Start = -1, protectedLine1End = -1; // Line 2 after prev button (empty line)
            int nextButtonStart = text.length();
            int protectedLine2Start = -1, protectedLine2End = -1; // ONLY the line immediately before next button
            
            if (text.startsWith("▲▲▲")) {
                prevButtonStart = 0;
                int firstNewline = text.indexOf("\n");
                if (firstNewline > 0) {
                    prevButtonEnd = firstNewline + 1; // Include the newline
                    protectedLine1Start = firstNewline + 1; // Start of line 2 (empty line)
                    int secondNewline = text.indexOf("\n", firstNewline + 1);
                    if (secondNewline > 0) {
                        protectedLine1End = secondNewline + 1; // End of line 2 including newline
                    }
                }
            }
            
            if (text.endsWith("▼▼▼")) {
                int buttonStart = text.lastIndexOf("\n\n▼▼▼");
                if (buttonStart > 0) {
                    // Only the SECOND empty line (immediately before ▼▼▼) is protected
                    protectedLine2Start = buttonStart + 1; // Start of second \n
                    protectedLine2End = buttonStart + 2; // End of second \n
                    nextButtonStart = buttonStart + 2;
                }
            }
            
            // BLOCK ALL EVENTS on protected lines - check FIRST before button handling
            if (protectedLine1Start >= 0 && offset >= protectedLine1Start && offset < protectedLine1End) {
                return true; // Consume ALL events on this line
            }
            if (protectedLine2Start >= 0 && offset >= protectedLine2Start && offset < protectedLine2End) {
                return true; // Consume ALL events on this line
            }
            
            // Handle button clicks on ACTION_DOWN
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                // Check if clicked on Previous button (entire ▲▲▲ line)
                if (offset >= prevButtonStart && offset < prevButtonEnd && prevButtonEnd > 0) {
                    updateFullContentFromChunk();
                    if (useLineBasedChunking) {
                        int prevLine = Math.max(0, currentChunkLine - CHUNK_LINES);
                        loadChunkWithButtons(prevLine);
                    } else {
                        int prevChunk = Math.max(0, currentChunkStart - CHUNK_SIZE);
                        loadChunkWithButtons(prevChunk);
                    }
                    return true; // Consume event
                }
                
                // Check if clicked on Next button
                if (offset >= nextButtonStart && offset <= text.length() && nextButtonStart < text.length()) {
                    updateFullContentFromChunk();
                    if (useLineBasedChunking) {
                        int nextLine = currentChunkLine + CHUNK_LINES;
                        loadChunkWithButtons(nextLine);
                    } else {
                        int nextChunk = currentChunkStart + CHUNK_SIZE;
                        loadChunkWithButtons(nextChunk);
                    }
                    return true; // Consume event
                }
            }
            
            return false;
        });
        
        // Monitor cursor position and force it away from protected lines INSTANTLY
        editor.setOnClickListener(v -> {
            if (!isLargeFile) return;
            
            // Check cursor position immediately after click
            android.os.Handler handler = new android.os.Handler();
            handler.post(() -> {
                String text = editor.getText().toString();
                int cursor = editor.getSelectionStart();
                
                int protectedLine1Start = -1, protectedLine1End = -1;
                int protectedLine2Start = -1, protectedLine2End = -1;
                
                if (text.startsWith("▲▲▲")) {
                    int firstNewline = text.indexOf("\n");
                    if (firstNewline > 0) {
                        protectedLine1Start = firstNewline + 1;
                        int secondNewline = text.indexOf("\n", firstNewline + 1);
                        if (secondNewline > 0) {
                            protectedLine1End = secondNewline + 1;
                        }
                    }
                }
                
                if (text.endsWith("▼▼▼")) {
                    int buttonStart = text.lastIndexOf("\n\n▼▼▼");
                    if (buttonStart > 0) {
                        protectedLine2Start = buttonStart + 1;
                        protectedLine2End = buttonStart + 2;
                    }
                }
                
                // Move cursor instantly
                if (protectedLine1Start >= 0 && cursor >= protectedLine1Start && cursor < protectedLine1End) {
                    editor.setSelection(Math.min(protectedLine1End, text.length()));
                } else if (protectedLine2Start >= 0 && cursor >= protectedLine2Start && cursor < protectedLine2End) {
                    editor.setSelection(Math.max(0, protectedLine2Start - 1));
                }
            });
        });
        
        // Periodic check - runs every 50ms to catch any cursor movement
        final android.os.Handler periodicHandler = new android.os.Handler();
        final Runnable periodicCheck = new Runnable() {
            @Override
            public void run() {
                if (!isLargeFile) {
                    periodicHandler.postDelayed(this, 50);
                    return;
                }
                
                String text = editor.getText().toString();
                int cursor = editor.getSelectionStart();
                
                int prevButtonStart = -1, prevButtonEnd = -1; // ▲▲▲ line
                int protectedLine1Start = -1, protectedLine1End = -1; // Empty line after ▲▲▲
                int nextButtonStart = -1, nextButtonEnd = -1; // ▼▼▼ line
                int protectedLine2Start = -1, protectedLine2End = -1; // Empty line before ▼▼▼
                
                if (text.startsWith("▲▲▲")) {
                    prevButtonStart = 0;
                    int firstNewline = text.indexOf("\n");
                    if (firstNewline > 0) {
                        prevButtonEnd = firstNewline + 1;
                        protectedLine1Start = firstNewline + 1;
                        int secondNewline = text.indexOf("\n", firstNewline + 1);
                        if (secondNewline > 0) {
                            protectedLine1End = secondNewline + 1;
                        }
                    }
                }
                
                if (text.endsWith("▼▼▼")) {
                    int buttonStart = text.lastIndexOf("\n\n▼▼▼");
                    if (buttonStart > 0) {
                        protectedLine2Start = buttonStart + 1;
                        protectedLine2End = buttonStart + 2;
                        nextButtonStart = buttonStart + 2;
                        nextButtonEnd = text.length();
                    }
                }
                
                // Move cursor instantly if in ANY protected zone
                if (prevButtonStart >= 0 && cursor >= prevButtonStart && cursor < prevButtonEnd) {
                    // On ▲▲▲ line - move to first code line
                    editor.setSelection(Math.min(prevButtonEnd + 1, text.length()));
                } else if (protectedLine1Start >= 0 && cursor >= protectedLine1Start && cursor < protectedLine1End) {
                    // On empty line after ▲▲▲ - move to first code line
                    editor.setSelection(Math.min(protectedLine1End, text.length()));
                } else if (protectedLine2Start >= 0 && cursor >= protectedLine2Start && cursor < protectedLine2End) {
                    // On empty line before ▼▼▼ - move to last code line
                    editor.setSelection(Math.max(0, protectedLine2Start - 1));
                } else if (nextButtonStart >= 0 && cursor >= nextButtonStart && cursor <= nextButtonEnd) {
                    // On ▼▼▼ line - move to last code line
                    editor.setSelection(Math.max(0, protectedLine2Start - 1));
                }
                
                // Schedule next check
                periodicHandler.postDelayed(this, 50);
            }
        };
        periodicHandler.post(periodicCheck);
        
        // Also monitor with TextWatcher for keyboard navigation
        editor.addTextChangedListener(new android.text.TextWatcher() {
            private boolean isMoving = false;
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (!isLargeFile || isMoving) return;
                
                // Check cursor position BEFORE any change
                String text = s.toString();
                int cursor = editor.getSelectionStart();
                
                int protectedLine1Start = -1, protectedLine1End = -1;
                int protectedLine2Start = -1, protectedLine2End = -1;
                
                if (text.startsWith("▲▲▲")) {
                    int firstNewline = text.indexOf("\n");
                    if (firstNewline > 0) {
                        protectedLine1Start = firstNewline + 1;
                        int secondNewline = text.indexOf("\n", firstNewline + 1);
                        if (secondNewline > 0) {
                            protectedLine1End = secondNewline + 1;
                        }
                    }
                }
                
                if (text.endsWith("▼▼▼")) {
                    int buttonStart = text.lastIndexOf("\n\n▼▼▼");
                    if (buttonStart > 0) {
                        protectedLine2Start = buttonStart + 1;
                        protectedLine2End = buttonStart + 2;
                    }
                }
                
                // Move cursor INSTANTLY if in protected zone
                if (protectedLine1Start >= 0 && cursor >= protectedLine1Start && cursor < protectedLine1End) {
                    isMoving = true;
                    editor.setSelection(Math.min(protectedLine1End, text.length()));
                    isMoving = false;
                } else if (protectedLine2Start >= 0 && cursor >= protectedLine2Start && cursor < protectedLine2End) {
                    isMoving = true;
                    editor.setSelection(Math.max(0, protectedLine2Start - 1));
                    isMoving = false;
                }
            }
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(android.text.Editable s) {
                if (!isLargeFile || isMoving) return;
                
                String text = s.toString();
                int cursor = editor.getSelectionStart();
                
                int protectedLine1Start = -1, protectedLine1End = -1;
                int protectedLine2Start = -1, protectedLine2End = -1;
                
                if (text.startsWith("▲▲▲")) {
                    int firstNewline = text.indexOf("\n");
                    if (firstNewline > 0) {
                        protectedLine1Start = firstNewline + 1;
                        int secondNewline = text.indexOf("\n", firstNewline + 1);
                        if (secondNewline > 0) {
                            protectedLine1End = secondNewline + 1;
                        }
                    }
                }
                
                if (text.endsWith("▼▼▼")) {
                    int buttonStart = text.lastIndexOf("\n\n▼▼▼");
                    if (buttonStart > 0) {
                        protectedLine2Start = buttonStart + 1;
                        protectedLine2End = buttonStart + 2;
                    }
                }
                
                // Force cursor away from protected lines
                if (protectedLine1Start >= 0 && cursor >= protectedLine1Start && cursor < protectedLine1End) {
                    isMoving = true;
                    editor.setSelection(Math.min(protectedLine1End, text.length()));
                    isMoving = false;
                } else if (protectedLine2Start >= 0 && cursor >= protectedLine2Start && cursor < protectedLine2End) {
                    isMoving = true;
                    editor.setSelection(Math.max(0, protectedLine2Start - 1));
                    isMoving = false;
                }
            }
        });
    }
    
    private void updateFullContentFromChunk() {
        if (!isLargeFile || fullFileContent.isEmpty()) return;
        
        String currentText = editor.getText().toString();
        
        // Remove load buttons from text (with part numbers)
        currentText = currentText.replaceFirst("^▲▲▲ TAP TO LOAD PREVIOUS \\(\\d+/\\d+\\) ▲▲▲\\n\\n", "");
        currentText = currentText.replaceFirst("\\n\\n▼▼▼ TAP TO LOAD NEXT \\(\\d+/\\d+\\) ▼▼▼$", "");
        
        // Calculate chunk end position
        int chunkEnd;
        if (useLineBasedChunking) {
            // For line-based, calculate end position from line count
            String[] allLines = fullFileContent.split("\n", -1);
            int endLine = Math.min(currentChunkLine + CHUNK_LINES, allLines.length);
            chunkEnd = 0;
            for (int i = 0; i < endLine; i++) {
                chunkEnd += allLines[i].length() + 1;
            }
            chunkEnd = Math.min(chunkEnd, fullFileContent.length());
        } else {
            // For character-based
            chunkEnd = Math.min(currentChunkStart + CHUNK_SIZE, fullFileContent.length());
        }
        
        String before = currentChunkStart > 0 ? fullFileContent.substring(0, currentChunkStart) : "";
        String after = chunkEnd < fullFileContent.length() ? fullFileContent.substring(chunkEnd) : "";
        fullFileContent = before + currentText + after;
    }
    
    private void saveFileState(File file) {
        SharedPreferences filePrefs = getSharedPreferences("GitCodeFiles", MODE_PRIVATE);
        filePrefs.edit().putString("lastFile_" + projectName, file.getAbsolutePath()).apply();
    }
    
    private void updateTabsAndUI(File file) {
        // Add to tabs if not already open
        if (!openTabs.contains(file)) {
            openTabs.add(file);
            updateTabBar();
        } else {
            highlightActiveTab(file);
        }
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(file.getName());
        }
        drawerLayout.closeDrawer(Gravity.START);
    }

    private void updateTabBar() {
        SharedPreferences themePrefs = getSharedPreferences("GitCodeTheme", MODE_PRIVATE);
        boolean isDark = themePrefs.getBoolean("darkMode", false);
        
        tabBar.removeAllViews();
        for (File file : openTabs) {
            LinearLayout tab = new LinearLayout(this);
            tab.setOrientation(LinearLayout.HORIZONTAL);
            tab.setPadding(15, 10, 15, 10);
            tab.setBackgroundColor(file.equals(currentFile) ? 
                (isDark ? 0xFF1E1E1E : 0xFFFFFFFF) : 
                (isDark ? 0xFF2D2D2D : 0xFFE0E0E0));
            
            TextView tabText = new TextView(this);
            tabText.setText(file.getName());
            tabText.setTextColor(isDark ? 0xFFE0E0E0 : 0xFF000000);
            tabText.setTextSize(14);
            tabText.setPadding(0, 0, 10, 0);
            tab.addView(tabText);
            
            TextView closeBtn = new TextView(this);
            closeBtn.setText("×");
            closeBtn.setTextSize(18);
            closeBtn.setTextColor(isDark ? 0xFFE0E0E0 : 0xFF000000);
            closeBtn.setOnClickListener(v -> closeTab(file));
            tab.addView(closeBtn);
            
            tab.setOnClickListener(v -> {
                // Auto-save if clicking same tab
                if (currentFile != null && currentFile.equals(file)) {
                    autoSaveFile();
                } else {
                    openFile(file);
                }
            });
            tabBar.addView(tab);
        }
    }

    private void highlightActiveTab(File file) {
        SharedPreferences themePrefs = getSharedPreferences("GitCodeTheme", MODE_PRIVATE);
        boolean isDark = themePrefs.getBoolean("darkMode", false);
        
        for (int i = 0; i < tabBar.getChildCount() && i < openTabs.size(); i++) {
            try {
                LinearLayout tab = (LinearLayout) tabBar.getChildAt(i);
                tab.setBackgroundColor(openTabs.get(i).equals(file) ? 
                    (isDark ? 0xFF1E1E1E : 0xFFFFFFFF) : 
                    (isDark ? 0xFF2D2D2D : 0xFFE0E0E0));
            } catch (Exception e) {
                // Skip if index mismatch
            }
        }
    }

    private void closeTab(File file) {
        openTabs.remove(file);
        if (file.equals(currentFile)) {
            if (!openTabs.isEmpty()) {
                openFile(openTabs.get(openTabs.size() - 1));
            } else {
                currentFile = null;
                editor.setText("No files open.\n\nTap the menu icon (☰) to open a file.");
                editor.setEnabled(false);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setSubtitle("");
                }
            }
        }
        updateTabBar();
    }

    private void updateLineNumbers(TextView lineNumbers, String text) {
        int lines = text.isEmpty() ? 1 : text.split("\n", -1).length;
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= lines; i++) {
            sb.append(i);
            if (i < lines) sb.append("\n");
        }
        lineNumbers.setText(sb.toString());
        
        // Measure actual text width for PERFECT fit
        lineNumbers.post(() -> {
            lineNumbers.measure(0, 0);
            int measuredWidth = lineNumbers.getMeasuredWidth();
            int perfectWidth = measuredWidth + (int)(4 * getResources().getDisplayMetrics().density);
            lineNumberScroll.getLayoutParams().width = perfectWidth;
            lineNumberScroll.requestLayout();
        });
        
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
        btn.setTextSize(17);
        btn.setPadding(15, 0, 15, 0);
        btn.setMinWidth(0);
        btn.setMinimumWidth(0);
        btn.setOnClickListener(listener);
        btn.setBackgroundColor(0x00000000);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(8, 0, 8, 0);
        btn.setLayoutParams(params);
        toolbar.addView(btn);
    }

    
    private void applySyntaxHighlightingForLargeFile() {
        if (currentFile == null) return;
        
        String text = editor.getText().toString();
        
        // Find button boundaries
        int codeStart = 0;
        int codeEnd = text.length();
        
        if (text.startsWith("▲▲▲")) {
            int endOfPrevButton = text.indexOf("\n\n");
            if (endOfPrevButton > 0) {
                codeStart = endOfPrevButton + 2;
            }
        }
        
        if (text.endsWith("▼▼▼")) {
            int startOfNextButton = text.lastIndexOf("\n\n▼▼▼");
            if (startOfNextButton > 0) {
                codeEnd = startOfNextButton;
            }
        }
        
        // Extract code content
        String codeContent = text.substring(codeStart, codeEnd);
        
        // Apply syntax highlighting to code content
        android.text.Spannable spannable = new android.text.SpannableStringBuilder(text);
        applySyntaxHighlightingToSpannable(currentFile.getName(), codeContent, spannable, codeStart);
        
        // Set the highlighted text
        int cursorPos = editor.getSelectionStart();
        editor.setText(spannable);
        
        // Restore cursor position
        if (cursorPos >= 0 && cursorPos <= spannable.length()) {
            editor.setSelection(Math.min(cursorPos, spannable.length()));
        }
    }
    
    private void applySyntaxHighlightingToSpannable(String fileName, String content, android.text.Spannable spannable, int offset) {
        if (content == null || content.isEmpty()) return;
        
        SharedPreferences themePrefs = getSharedPreferences("GitCodeTheme", MODE_PRIVATE);
        boolean isDark = themePrefs.getBoolean("darkMode", false);
        
        String ext = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            ext = fileName.substring(i + 1).toLowerCase();
        }
        
        // Only apply syntax highlighting for code files
        String[] codeExtensions = {"java", "js", "jsx", "ts", "tsx", "py", "html", "xml", "css", "scss", "sass", 
                                    "c", "cpp", "h", "hpp", "go", "rs", "php", "rb", "swift", "kt", "json", "yaml", "yml"};
        boolean isCodeFile = false;
        for (String codeExt : codeExtensions) {
            if (ext.equals(codeExt)) {
                isCodeFile = true;
                break;
            }
        }
        
        if (!isCodeFile) return;
        
        try {
            String[] keywords = getKeywordsForExtension(ext);
            int keywordColor = isDark ? 0xFFFF79C6 : 0xFF0000FF;
            int stringColor = isDark ? 0xFF50FA7B : 0xFF008000;
            int commentColor = isDark ? 0xFF6272A4 : 0xFF808080;
            int numberColor = isDark ? 0xFFBD93F9 : 0xFFFF6600;
            int functionColor = isDark ? 0xFF8BE9FD : 0xFF0080FF;
            
            // Highlight keywords
            for (String keyword : keywords) {
                int start = 0;
                while ((start = content.indexOf(keyword, start)) != -1) {
                    if ((start == 0 || !Character.isLetterOrDigit(content.charAt(start - 1))) &&
                        (start + keyword.length() >= content.length() || !Character.isLetterOrDigit(content.charAt(start + keyword.length())))) {
                        spannable.setSpan(new android.text.style.ForegroundColorSpan(keywordColor),
                            offset + start, offset + start + keyword.length(),
                            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    start += keyword.length();
                }
            }
            
            highlightPatternWithOffset(spannable, content, "\\b\\d+\\.?\\d*\\b", numberColor, offset);
            highlightPatternWithOffset(spannable, content, "\\b\\w+(?=\\()", functionColor, offset);
            highlightPatternWithOffset(spannable, content, "\"[^\"]*\"", stringColor, offset);
            highlightPatternWithOffset(spannable, content, "'[^']*'", stringColor, offset);
            highlightPatternWithOffset(spannable, content, "`[^`]*`", stringColor, offset);
            highlightPatternWithOffset(spannable, content, "//.*", commentColor, offset);
            highlightPatternWithOffset(spannable, content, "#.*", commentColor, offset);
            highlightPatternWithOffset(spannable, content, "/\\*.*?\\*/", commentColor, offset);
        } catch (Exception e) {
            // Skip if highlighting fails
        }
    }
    
    private void highlightPatternWithOffset(android.text.Spannable spannable, String content, String regex, int color, int offset) {
        try {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
            java.util.regex.Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                spannable.setSpan(new android.text.style.ForegroundColorSpan(color),
                    offset + matcher.start(), offset + matcher.end(),
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        } catch (Exception e) {
            // Skip if pattern fails
        }
    }
    
    private void applySyntaxHighlighting(String fileName, String content) {
        if (content == null || content.isEmpty()) {
            editor.setText("");
            return;
        }
        
        SharedPreferences themePrefs = getSharedPreferences("GitCodeTheme", MODE_PRIVATE);
        boolean isDark = themePrefs.getBoolean("darkMode", false);
        
        String ext = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            ext = fileName.substring(i + 1).toLowerCase();
        }
        
        // Only apply syntax highlighting for code files
        String[] codeExtensions = {"java", "js", "jsx", "ts", "tsx", "py", "html", "xml", "css", "scss", "sass", 
                                    "c", "cpp", "h", "hpp", "go", "rs", "php", "rb", "swift", "kt", "json", "yaml", "yml"};
        boolean isCodeFile = false;
        for (String codeExt : codeExtensions) {
            if (ext.equals(codeExt)) {
                isCodeFile = true;
                break;
            }
        }
        
        if (!isCodeFile) {
            editor.setText(content);
            return;
        }
        
        try {
            android.text.SpannableString spannable = new android.text.SpannableString(content);
            
            String[] keywords = getKeywordsForExtension(ext);
            int keywordColor = isDark ? 0xFFFF79C6 : 0xFF0000FF;  // Pink/Blue
            int stringColor = isDark ? 0xFF50FA7B : 0xFF008000;   // Green
            int commentColor = isDark ? 0xFF6272A4 : 0xFF808080;  // Gray
            int numberColor = isDark ? 0xFFBD93F9 : 0xFFFF6600;   // Purple/Orange
            int functionColor = isDark ? 0xFF8BE9FD : 0xFF0080FF; // Cyan/Blue
            
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
            
            // Highlight numbers
            highlightPattern(spannable, content, "\\b\\d+\\.?\\d*\\b", numberColor);
            
            // Highlight function calls
            highlightPattern(spannable, content, "\\b\\w+(?=\\()", functionColor);
            
            // Highlight strings (must be after keywords to override)
            highlightPattern(spannable, content, "\"[^\"]*\"", stringColor);
            highlightPattern(spannable, content, "'[^']*'", stringColor);
            highlightPattern(spannable, content, "`[^`]*`", stringColor);
            
            // Highlight comments (must be last to override everything)
            highlightPattern(spannable, content, "//.*", commentColor);
            highlightPattern(spannable, content, "#.*", commentColor);
            highlightPattern(spannable, content, "/\\*.*?\\*/", commentColor);
            
            // Save cursor position
            int cursorPos = editor.getSelectionStart();
            editor.setText(spannable);
            // Restore cursor position
            if (cursorPos >= 0 && cursorPos <= spannable.length()) {
                editor.setSelection(cursorPos);
            }
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
        AlertDialog.Builder builder = createThemedDialog();
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
        showThemedDialog(builder);
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
            if (isLargeFile && !fullFileContent.isEmpty()) {
                // Update full content from current chunk first
                updateFullContentFromChunk();
                fos.write(fullFileContent.getBytes());
            } else {
                fos.write(editor.getText().toString().getBytes());
            }
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
            Toast.makeText(this, "Configure GitHub profile first", Toast.LENGTH_LONG).show();
            return;
        }
        
        AlertDialog.Builder builder = createThemedDialog();
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
        showThemedDialog(builder);
    }

    private void pushAllToGitHub(String username, String token, String repo, String message) {
        Toast.makeText(this, "Pushing changes...", Toast.LENGTH_SHORT).show();
        executor.execute(() -> {
            GitHubAPI api = new GitHubAPI(username, token, repo);
            
            // Check if repo exists
            String repoCheck = api.checkRepoExists();
            if (repoCheck.contains("Error: 404")) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Repository '" + repo + "' not found on GitHub", Toast.LENGTH_LONG).show();
                });
                return;
            } else if (repoCheck.contains("Error")) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Failed to connect: " + repoCheck, Toast.LENGTH_LONG).show();
                });
                return;
            }
            
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
                    // Only push .gitkeep if it doesn't exist on remote yet
                    if (gitkeepTime == 0) {
                        String result = api.commitAndPush(gitkeepPath, "", message);
                        if (result.contains("Success")) {
                            pushPrefs.edit().putLong(gitkeepPath, System.currentTimeMillis()).apply();
                            success++;
                        }
                    }
                } else {
                    success += pushModifiedFiles(api, file, filePath, message, results, pushPrefs, currentFiles);
                }
            } else {
                long fileModified = file.lastModified();
                currentFiles.add(filePath);
                
                long lastPushed = pushPrefs.getLong(filePath, 0);
                String lastHash = pushPrefs.getString(filePath + "_hash", "");
                
                if (fileModified > lastPushed) {
                    try {
                        FileInputStream fis = new FileInputStream(file);
                        byte[] data = new byte[(int) file.length()];
                        fis.read(data);
                        fis.close();
                        
                        String content = new String(data);
                        String currentHash = String.valueOf(content.hashCode());
                        
                        // Only push if content actually changed
                        if (!currentHash.equals(lastHash)) {
                            String result = api.commitAndPush(filePath, content, message);
                            if (result.contains("Success")) {
                                pushPrefs.edit()
                                    .putLong(filePath, fileModified)
                                    .putString(filePath + "_hash", currentHash)
                                    .apply();
                                success++;
                            }
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

    private void highlightMatchingBracket() {
        // Clear previous highlights
        android.text.Spannable spannable = editor.getText();
        android.text.style.BackgroundColorSpan[] spans = spannable.getSpans(0, spannable.length(), android.text.style.BackgroundColorSpan.class);
        for (android.text.style.BackgroundColorSpan span : spans) {
            if (spannable.getSpanStart(span) != -1) {
                spannable.removeSpan(span);
            }
        }
        
        int pos = editor.getSelectionStart();
        String text = editor.getText().toString();
        
        if (pos < 0 || pos >= text.length()) return;
        
        char ch = text.charAt(pos);
        char match = 0;
        boolean forward = false;
        
        switch (ch) {
            case '(': match = ')'; forward = true; break;
            case '[': match = ']'; forward = true; break;
            case '{': match = '}'; forward = true; break;
            case ')': match = '('; break;
            case ']': match = '['; break;
            case '}': match = '{'; break;
        }
        
        if (match == 0) return;
        
        int matchPos = findMatchingBracket(text, pos, ch, match, forward);
        if (matchPos != -1) {
            SharedPreferences themePrefs = getSharedPreferences("GitCodeTheme", MODE_PRIVATE);
            boolean isDark = themePrefs.getBoolean("darkMode", true);
            int highlightColor = isDark ? 0x4400FF00 : 0x4400FF00;
            
            spannable.setSpan(new android.text.style.BackgroundColorSpan(highlightColor),
                pos, pos + 1, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new android.text.style.BackgroundColorSpan(highlightColor),
                matchPos, matchPos + 1, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private int findMatchingBracket(String text, int pos, char open, char close, boolean forward) {
        int count = 1;
        int i = forward ? pos + 1 : pos - 1;
        
        while (forward ? i < text.length() : i >= 0) {
            char ch = text.charAt(i);
            if (ch == open) count++;
            else if (ch == close) count--;
            
            if (count == 0) return i;
            i += forward ? 1 : -1;
        }
        return -1;
    }

    private void showProjectStats() {
        File dir = new File(projectPath);
        int[] stats = calculateStats(dir);
        
        AlertDialog.Builder builder = createThemedDialog();
        builder.setTitle("Project Statistics");
        
        TextView tv = new TextView(this);
        tv.setPadding(50, 40, 50, 40);
        tv.setText(
            "Files: " + stats[0] + "\n" +
            "Folders: " + stats[1] + "\n" +
            "Lines of Code: " + stats[2] + "\n" +
            "Characters: " + stats[3]
        );
        tv.setTextSize(16);
        
        builder.setView(tv);
        builder.setPositiveButton("OK", null);
        showThemedDialog(builder);
    }

    private int[] calculateStats(File dir) {
        int[] stats = new int[4]; // files, folders, lines, chars
        File[] files = dir.listFiles();
        if (files == null) return stats;
        
        for (File file : files) {
            if (file.isDirectory()) {
                stats[1]++;
                int[] subStats = calculateStats(file);
                stats[0] += subStats[0];
                stats[1] += subStats[1];
                stats[2] += subStats[2];
                stats[3] += subStats[3];
            } else {
                stats[0]++;
                try {
                    FileInputStream fis = new FileInputStream(file);
                    byte[] data = new byte[(int) file.length()];
                    fis.read(data);
                    fis.close();
                    String content = new String(data);
                    stats[2] += content.split("\n").length;
                    stats[3] += content.length();
                } catch (Exception e) {
                    // Skip
                }
            }
        }
        return stats;
    }

    private void pullFromGitHub() {
        String username = prefs.getString("username", "");
        String token = prefs.getString("token", "");
        
        if (username.isEmpty() || token.isEmpty()) {
            Toast.makeText(this, "Configure GitHub profile first", Toast.LENGTH_LONG).show();
            return;
        }
        
        Toast.makeText(this, "Pulling from GitHub...", Toast.LENGTH_SHORT).show();
        executor.execute(() -> {
            GitHubAPI api = new GitHubAPI(username, token, projectName);
            
            // Check if repo exists
            String repoCheck = api.checkRepoExists();
            if (repoCheck.contains("Error: 404")) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Repository '" + projectName + "' not found on GitHub", Toast.LENGTH_LONG).show();
                });
                return;
            } else if (repoCheck.contains("Error")) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Failed to connect: " + repoCheck, Toast.LENGTH_LONG).show();
                });
                return;
            }
            
            // Get all files from repo
            java.util.List<String> files = api.getRepoTree();
            
            if (files.isEmpty()) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Repository is empty or couldn't fetch files", Toast.LENGTH_LONG).show();
                });
                return;
            }
            
            int pulled = 0;
            
            for (String filePath : files) {
                String content = api.pullFile(filePath);
                if (content != null) {
                    File file = new File(projectPath, filePath);
                    file.getParentFile().mkdirs();
                    try {
                        // Overwrite local file with remote content
                        java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
                        fos.write(content.getBytes());
                        fos.close();
                        pulled++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            
            int finalPulled = pulled;
            runOnUiThread(() -> {
                if (finalPulled > 0) {
                    Toast.makeText(this, "✓ Pulled " + finalPulled + " files from GitHub", Toast.LENGTH_LONG).show();
                    loadFiles();
                    if (currentFile != null && currentFile.exists()) {
                        openFile(currentFile);
                    }
                } else {
                    Toast.makeText(this, "Failed to pull files", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private int pullAllFiles(GitHubAPI api, File dir, String relativePath) {
        int count = 0;
        File[] files = dir.listFiles();
        if (files == null) return 0;
        
        for (File file : files) {
            String filePath = relativePath.isEmpty() ? file.getName() : relativePath + "/" + file.getName();
            
            if (file.isDirectory()) {
                count += pullAllFiles(api, file, filePath);
            } else {
                String content = api.pullFile(filePath);
                if (content != null) {
                    try {
                        java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
                        fos.write(content.getBytes());
                        fos.close();
                        count++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return count;
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
