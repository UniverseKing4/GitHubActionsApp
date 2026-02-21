# GitCode Developer Guide - UI/UX

## Quick Start

### Using the New UI System

#### 1. Apply Theme to Your Activity

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    // Update theme cache
    ThemeHelper.updateDarkModeCache(this);
    
    // Apply theme to window
    ThemeHelper.applyThemeToWindow(getWindow(), ThemeHelper.isDarkMode(this));
    
    setContentView(R.layout.activity_your_activity);
}
```

#### 2. Create a Styled Dialog

```java
// Build your input view
EditText input = new ThemeHelper.ThemedDialog(this).createStyledEditText("Enter text...");

// Show themed dialog
new ThemeHelper.ThemedDialog(this)
    .setTitle("Dialog Title")
    .setView(input)
    .setPositiveButton("OK", (dialog, which) -> {
        String text = input.getText().toString();
        // Handle OK
    })
    .setNegativeButton("Cancel", null)
    .show();
```

#### 3. Build UI Components with ComponentBuilder

```java
ComponentBuilder builder = new ComponentBuilder(this);

// Create a styled button
Button saveButton = builder.button()
    .text("Save")
    .style(ComponentBuilder.ButtonBuilder.ButtonStyle.PRIMARY)
    .onClick(v -> saveFile())
    .build();

// Create a styled text input
EditText filenameInput = builder.textInput()
    .hint("Filename")
    .inputType(InputType.TYPE_CLASS_TEXT)
    .build();

// Create a styled text view
TextView titleView = builder.textView()
    .text("Section Title")
    .type(ComponentBuilder.TextViewBuilder.TextType.HEADLINE_6)
    .bold(true)
    .build();

// Create a styled card container
LinearLayout card = builder.card()
    .padding(16)
    .cornerRadius(12f)
    .clickable(true)
    .onClick(v -> handleCardClick())
    .buildContainer();

// Add views to card
card.addView(titleView);
card.addView(filenameInput);
card.addView(saveButton);
```

#### 4. Animate Views with AnimationHelper

```java
// Fade in a view
AnimationHelper.fadeIn(view, AnimationHelper.DURATION_NORMAL);

// Scale in a dialog
AnimationHelper.scaleIn(dialogView, AnimationHelper.DURATION_NORMAL);

// Pulse effect on button click
button.setOnClickListener(v -> {
    AnimationHelper.pulse(v);
    // Handle click
});

// Stagger animation for list items
View[] items = {item1, item2, item3, item4};
AnimationHelper.stagger(items, 50, AnimationHelper.AnimationType.SLIDE_UP);

// Expand/collapse views
AnimationHelper.expandHeight(view, targetHeight, 300);
AnimationHelper.collapseHeight(view, 300);
```

#### 5. Get Themed Colors

```java
// Get color based on current theme
int bgColor = ThemeHelper.getColor(this, "background");
int textColor = ThemeHelper.getColor(this, "on_surface");
int borderColor = ThemeHelper.getColor(this, "border");

// Or use predefined colors directly
int primaryColor = isDarkMode ? 0xFF4CAF50 : 0xFF2E7D32;
```

---

## Component Reference

### Button Styles

```java
// Primary button (filled)
Button primaryBtn = builder.button()
    .text("Primary Action")
    .style(ButtonStyle.PRIMARY)
    .build();

// Outline button
Button outlineBtn = builder.button()
    .text("Secondary Action")
    .style(ButtonStyle.OUTLINE)
    .build();

// Text button
Button textBtn = builder.button()
    .text("Tertiary Action")
    .style(ButtonStyle.TEXT)
    .build();
```

### Text View Types

```java
// Various text styles
textView().type(TextType.HEADLINE_1).build();    // 96sp
textView().type(TextType.HEADLINE_6).build();    // 20sp
textView().type(TextType.BODY).build();          // 16sp
textView().type(TextType.CAPTION).build();       // 12sp
textView().type(TextType.OVERLINE).build();      // 10sp
```

### Card Variations

```java
// Standard card
View card = builder.card()
    .backgroundColor(0xFF252525)
    .cornerRadius(12f)
    .padding(16)
    .build();

// Clickable card
View clickableCard = builder.card()
    .clickable(true)
    .onClick(v -> handleClick())
    .build();

// Card container for adding views
LinearLayout cardContainer = builder.card()
    .padding(16)
    .buildContainer();
cardContainer.addView(textView);
cardContainer.addView(button);
```

---

## Animation Reference

### Animation Types

```java
// Fade animations
AnimationHelper.fadeIn(view, 300);
AnimationHelper.fadeOut(view, 300);

// Scale animations
AnimationHelper.scaleIn(view, 300);
AnimationHelper.shrink(view, 300);

// Slide animations
AnimationHelper.slideUp(view, 300);
AnimationHelper.slideDown(view, 300);
AnimationHelper.slideInRight(view, 300);
AnimationHelper.slideOutRight(view, 300);

// Special effects
AnimationHelper.pulse(view);           // Button press
AnimationHelper.rippleEffect(view);    // Ripple feedback

// Height animations
AnimationHelper.expandHeight(view, 200, 300);
AnimationHelper.collapseHeight(view, 300);
```

### Animation Durations

```java
AnimationHelper.DURATION_FAST    // 150ms - Micro-interactions
AnimationHelper.DURATION_NORMAL  // 300ms - Standard transitions
AnimationHelper.DURATION_SLOW   // 500ms - Complex animations
```

### Staggered Animations

```java
View[] views = {view1, view2, view3, view4};

// Stagger with 50ms delay between each
AnimationHelper.stagger(
    views,
    50,  // Stagger delay
    AnimationHelper.AnimationType.FADE_IN
);

// Stagger with custom duration
AnimationHelper.stagger(
    views,
    50,
    AnimationHelper.AnimationType.SCALE_IN,
    300  // Animation duration
);
```

---

## Color Reference

### Dark Theme Colors

```java
// Brand
0xFF4CAF50  // Primary
0xFF2E7D32  // Primary Container

// Backgrounds
0xFF121212  // Background
0xFF1E1E1E  // Surface
0xFF252525  // Surface Variant
0xFF2D2D2D  // Surface Elevated

// Text
0xFFE0E0E0  // On Background / On Surface
0xFFB0B0B0  // On Surface Variant
0xFF757575  // Text Hint

// Editor
0xFF1A1A1A  // Editor Background
0xFFE8E8E8  // Editor Text
0xFF252525  // Line Numbers Bg
0xFF6B7280  // Line Numbers Text
0x6633B5E5  // Selection

// Syntax Highlighting
0xFFC792EA  // Keywords
0xFFA5D6A7  // Strings
0xFFFD9175  // Numbers
0xFF6A737D  // Comments
0xFF82AAFF  // Functions
0xFF7DD3FC  // Types
0xFFF472B6  // Tags
0xFFBBF7D0  // Attributes

// Status
0xFF4CAF50  // Success
0xFFFF9800  // Warning
0xFFF44336  // Error
0xFF2196F3  // Info
```

### Light Theme Colors

```java
// Brand
0xFF2E7D32  // Primary
0xFF4CAF50  // Primary Container

// Backgrounds
0xFFFAFAFA  // Background
0xFFFFFFFF  // Surface
0xFFF5F5F5  // Surface Variant

// Text
0xFF121212  // On Background / On Surface
0xFF6B7280  // On Surface Variant
0xFF9E9E9E  // Text Hint

// Editor
0xFFFFFFFF  // Editor Background
0xFF1F2937  // Editor Text
0xFFF5F5F5  // Line Numbers Bg
0xFF9CA3AF  // Line Numbers Text
0x802196F3  // Selection

// Syntax Highlighting
0xFF7C3AED  // Keywords
0xFF059669  // Strings
0xFFDC2626  // Numbers
0xFF6B737D  // Comments
0xFF2563EB  // Functions
0xFF0891B2  // Types
0xFFDB2777  // Tags
0xFF65A30D  // Attributes

// Status
0xFF2E7D32  // Success
0xFFF59E0B  // Warning
0xFFDC2626  // Error
0xFF0284C7  // Info
```

---

## Layout Tips

### Using the New Layouts

#### ProjectsActivity Layout

The new `activity_projects.xml` includes:

- **Brand Section**: App title and subtitle
- **Quick Actions**: Cards for New Project, Clone, Profiles, Settings
- **Projects List**: Container for project items
- **Empty State**: Shown when no projects exist

Connect your views:

```java
// Find card views
MaterialCardView cardNewProject = findViewById(R.id.cardNewProject);
MaterialCardView cardClone = findViewById(R.id.cardClone);
MaterialCardView cardProfiles = findViewById(R.id.cardProfiles);
MaterialCardView cardSettings = findViewById(R.id.cardSettings);

// Set click listeners
cardNewProject.setOnClickListener(v -> createProject());
cardClone.setOnClickListener(v -> cloneRepo());
cardProfiles.setOnClickListener(v -> showProfiles());
cardSettings.setOnClickListener(v -> showSettings());

// Get list and empty state
LinearLayout projectsList = findViewById(R.id.projectsList);
LinearLayout emptyState = findViewById(R.id.emptyState);
TextView projectCount = findViewById(R.id.tvProjectCount);
```

#### IDEActivity Layout

The new `activity_ide.xml` includes:

- **Tab Bar**: Horizontal scrolling tabs
- **Line Numbers**: Synced scroll with editor
- **Code Editor**: Syntax highlighting support
- **Status Bar**: File info, cursor position, encoding
- **File Drawer**: Organized file browser

Connect your views:

```java
// Find views
HorizontalScrollView tabScroll = findViewById(R.id.tabScroll);
LinearLayout tabBar = findViewById(R.id.tabBar);
ScrollView lineNumberScroll = findViewById(R.id.lineNumberScroll);
TextView lineNumbers = findViewById(R.id.lineNumbers);
ScrollView editorScroll = findViewById(R.id.editorScroll);
EditText editor = findViewById(R.id.editor);
TextView tvCurrentFile = findViewById(R.id.tvCurrentFile);
TextView tvCursorPosition = findViewById(R.id.tvCursorPosition);
DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
LinearLayout drawer = findViewById(R.id.drawer);
TextView tvProjectName = findViewById(R.id.tvProjectName);
TextView tvProjectPath = findViewById(R.id.tvProjectPath);
Button btnNewFile = findViewById(R.id.btnNewFile);
Button btnNewFolder = findViewById(R.id.btnNewFolder);
Button btnSelect = findViewById(R.id.btnSelect);
LinearLayout fileList = findViewById(R.id.fileList);
```

---

## Best Practices

### 1. Always Use Theme Helper for Colors

```java
// Good
int bgColor = ThemeHelper.getColor(this, "background");
boolean isDark = ThemeHelper.isDarkMode(this);

// Avoid hardcoding
// int bgColor = 0xFF121212;  // Don't do this
```

### 2. Use Component Builder for Consistent UI

```java
// Good
Button button = builder.button()
    .text("Save")
    .style(ButtonStyle.PRIMARY)
    .onClick(v -> save())
    .build();

// Avoid manual styling
// Button button = new Button(this);
// button.setText("Save");
// button.setBackgroundColor(0xFF4CAF50);
// // ...
```

### 3. Animate View Transitions

```java
// Good
view.setVisibility(View.GONE);
AnimationHelper.fadeIn(view, 300);

// Avoid abrupt changes
// view.setVisibility(View.VISIBLE);
```

### 4. Handle Dark Mode Changes

```java
@Override
protected void onResume() {
    super.onResume();
    ThemeHelper.updateDarkModeCache(this);
    // Apply theme changes if needed
}
```

### 5. Use Themed Dialogs

```java
// Good
new ThemeHelper.ThemedDialog(this)
    .setTitle("Title")
    .setView(view)
    .show();

// Avoid unthemed dialogs
// new AlertDialog.Builder(this).show();
```

---

## Common Patterns

### Creating a Project List Item

```java
private View createProjectItem(String name, String path) {
    ComponentBuilder builder = new ComponentBuilder(this);
    
    LinearLayout card = builder.card()
        .padding(16)
        .cornerRadius(12f)
        .clickable(true)
        .onClick(v -> openProject(name, path))
        .buildContainer();
    
    // Add project info
    TextView title = builder.textView()
        .text(name)
        .type(TextType.HEADLINE_6)
        .bold(true)
        .build();
    
    TextView subtitle = builder.textView()
        .text(path)
        .type(TextType.CAPTION)
        .padding(0, 8, 0, 0)
        .build();
    
    // Add action buttons
    LinearLayout actions = builder.layout()
        .orientation(LinearLayout.HORIZONTAL)
        .padding(0, 16, 0, 0)
        .gravity(Gravity.END)
        .build();
    
    Button editBtn = builder.button()
        .text("Edit")
        .style(ButtonStyle.OUTLINE)
        .onClick(v -> editProject(name, path))
        .build();
    
    Button deleteBtn = builder.button()
        .text("Delete")
        .style(ButtonStyle.OUTLINE)
        .onClick(v -> deleteProject(name, path))
        .build();
    
    card.addView(title);
    card.addView(subtitle);
    actions.addView(editBtn);
    actions.addView(deleteBtn);
    card.addView(actions);
    
    return card;
}
```

### Creating a File List Item

```java
private View createFileItem(File file, boolean isFolder) {
    ComponentBuilder builder = new ComponentBuilder(this);
    
    LinearLayout item = builder.layout()
        .orientation(LinearLayout.HORIZONTAL)
        .padding(12, 8, 12, 8)
        .gravity(Gravity.CENTER_VERTICAL)
        .build();
    
    // Icon
    TextView icon = builder.textView()
        .text(isFolder ? "📁" : getFileIcon(file))
        .type(TextType.SUBTITLE_1)
        .build();
    
    // Name
    TextView name = builder.textView()
        .text(file.getName())
        .type(TextType.BODY)
        .padding(12, 0, 0, 0)
        .build();
    
    // Make item clickable
    item.setClickable(true);
    item.setOnClickListener(v -> handleFileClick(file));
    
    item.addView(icon);
    item.addView(name);
    
    return item;
}
```

### Creating a Form Dialog

```java
private void showCreateProjectDialog() {
    ComponentBuilder builder = new ComponentBuilder(this);
    
    // Create form container
    LinearLayout form = builder.layout()
        .orientation(LinearLayout.VERTICAL)
        .padding(0, 8, 0, 0)
        .build();
    
    // Project name input
    EditText nameInput = builder.textInput()
        .hint("Project Name")
        .build();
    
    // Path input
    EditText pathInput = builder.textInput()
        .hint("Path (optional)")
        .text(Environment.getExternalStorageDirectory() + "/GitCode/")
        .build();
    
    form.addView(nameInput);
    form.addView(pathInput);
    
    // Show dialog
    new ThemeHelper.ThemedDialog(this)
        .setTitle("New Project")
        .setView(form)
        .setPositiveButton("Create", (d, w) -> {
            String name = nameInput.getText().toString().trim();
            String path = pathInput.getText().toString().trim();
            createProject(name, path);
        })
        .setNegativeButton("Cancel", null)
        .show();
}
```

---

## Troubleshooting

### Theme Not Applying

**Problem**: Colors not updating after theme change

**Solution**: Always call `ThemeHelper.updateDarkModeCache()` in `onResume()`:

```java
@Override
protected void onResume() {
    super.onResume();
    ThemeHelper.updateDarkModeCache(this);
    recreate(); // If full update needed
}
```

### Animation Not Showing

**Problem**: Views appear instantly without animation

**Solution**: Ensure view is visible before animating:

```java
// Set visibility first
view.setVisibility(View.VISIBLE);
view.setAlpha(0f); // Start invisible
AnimationHelper.fadeIn(view, 300);
```

### Dialog Wrong Color

**Problem**: Dialog shows white background in dark mode

**Solution**: Use `ThemedDialog` helper:

```java
// Good
new ThemeHelper.ThemedDialog(this).show();

// Avoid
new AlertDialog.Builder(this).show();
```

---

## Additional Resources

- **Full Documentation**: `UI_REDESIGN_DOCUMENTATION.md`
- **Color Reference**: `app/src/main/res/values/colors.xml`
- **Theme Reference**: `app/src/main/res/values/themes.xml`
- **String Resources**: `app/src/main/res/values/strings.xml`
- **Layout Files**: `app/src/main/res/layout/`

---

*Last Updated: UI/UX Redesign v1.0*
