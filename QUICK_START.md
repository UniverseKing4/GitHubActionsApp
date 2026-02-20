# GitCode UI/UX - Quick Start Guide

## 🚀 Getting Started

### 1. Build the App
```bash
cd GitHubActionsApp-152
./build-redesign.sh
```

### 2. Install on Device
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 📚 Key Files to Know

### Design System
```
app/src/main/res/values/
├── colors.xml    ← All colors defined here
├── dimens.xml    ← All spacing/sizing here
└── styles.xml    ← All component styles here
```

### Layouts
```
app/src/main/res/layout/
├── activity_main.xml      ← GitHub integration screen
├── activity_ide.xml       ← Code editor screen
├── activity_projects.xml  ← Projects list screen
├── tab_item.xml          ← Editor tab component
├── file_item.xml         ← File explorer item
└── project_item.xml      ← Project card component
```

### Code
```
app/src/main/java/com/github/actions/
├── MainActivity.java     ← Main screen logic
├── IDEActivity.java      ← Editor logic
├── ProjectsActivity.java ← Projects logic
└── UIHelper.java         ← Animation utilities
```

## 🎨 Using the Design System

### Adding a New Color
```xml
<!-- In colors.xml -->
<color name="my_color">#FF5733</color>

<!-- In layout -->
android:textColor="@color/my_color"
```

### Adding a New Dimension
```xml
<!-- In dimens.xml -->
<dimen name="my_spacing">20dp</dimen>

<!-- In layout -->
android:padding="@dimen/my_spacing"
```

### Creating a New Style
```xml
<!-- In styles.xml -->
<style name="MyButton" parent="Button.Primary">
    <item name="android:textSize">16sp</item>
</style>

<!-- In layout -->
style="@style/MyButton"
```

## 🎭 Adding Animations

### Fade In a View
```java
UIHelper.fadeIn(myView);
```

### Fade Out a View
```java
UIHelper.fadeOut(myView, UIHelper.DURATION_SHORT, () -> {
    // Optional callback when animation completes
});
```

### Button Press Feedback
```java
button.setOnClickListener(v -> {
    UIHelper.scalePress(v);
    // Your click logic here
});
```

### Staggered List Animation
```java
View[] items = {item1, item2, item3};
UIHelper.staggeredFadeIn(items, 0);
```

## 🎯 Creating New Screens

### 1. Create Layout XML
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/surface_dark">
    
    <!-- Your content here -->
    
</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

### 2. Create Activity
```java
public class MyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        
        // Initialize views
        // Setup listeners
        // Animate content
        UIHelper.fadeIn(findViewById(R.id.content));
    }
}
```

### 3. Add to Manifest
```xml
<activity
    android:name=".MyActivity"
    android:exported="false" />
```

## 🎨 Common Patterns

### Card with Content
```xml
<com.google.android.material.card.MaterialCardView
    style="@style/Card"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="@dimen/spacing_lg">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="@dimen/spacing_md">
        
        <TextView
            style="@style/Text.Title"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Card Title" />
            
        <TextView
            style="@style/Text.Body"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="@dimen/spacing_sm"
            android:text="Card content goes here" />
            
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
```

### Primary Button
```xml
<com.google.android.material.button.MaterialButton
    android:id="@+id/myButton"
    style="@style/Button.Primary"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="Click Me"
    app:icon="@android:drawable/ic_input_add"
    app:iconGravity="start" />
```

### Text Input
```xml
<com.google.android.material.textfield.TextInputLayout
    style="@style/Input"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="Enter text">
    
    <com.google.android.material.textfield.TextInputEditText
        android:id="@+id/myInput"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:textColor="@color/text_primary_dark"
        android:textSize="@dimen/text_body" />
</com.google.android.material.textfield.TextInputLayout>
```

## 🔧 Customization

### Changing Primary Color
```xml
<!-- In colors.xml -->
<color name="primary">#YOUR_COLOR</color>
```

### Adjusting Spacing
```xml
<!-- In dimens.xml -->
<dimen name="spacing_md">YOUR_VALUE</dimen>
```

### Modifying Button Style
```xml
<!-- In styles.xml -->
<style name="Button.Primary" parent="Widget.MaterialComponents.Button">
    <item name="backgroundTint">@color/primary</item>
    <item name="cornerRadius">YOUR_RADIUS</item>
</style>
```

## 🐛 Troubleshooting

### Build Errors
```bash
# Clean and rebuild
./gradlew clean
./gradlew assembleDebug
```

### Layout Not Showing
- Check if view visibility is set to VISIBLE
- Verify parent layout has proper dimensions
- Check for layout_weight or layout_gravity issues

### Colors Not Applying
- Ensure using @color/ prefix
- Check if color is defined in colors.xml
- Verify theme is applied in manifest

### Animations Not Working
- Check if view is already visible
- Verify animation duration is reasonable
- Ensure view has proper dimensions

## 📖 Learning Resources

### Design System
- Read: `DESIGN_SYSTEM_GUIDE.md`
- Reference: `colors.xml`, `dimens.xml`, `styles.xml`

### Implementation Details
- Read: `UI_REDESIGN_SUMMARY.md`
- Check: `IMPLEMENTATION_CHECKLIST.md`

### Code Examples
- MainActivity.java - Material components usage
- UIHelper.java - Animation patterns
- Layout files - XML structure patterns

## 🎯 Best Practices

### DO
✓ Use design system colors and dimensions
✓ Follow 8dp spacing grid
✓ Apply proper styles to components
✓ Add animations for feedback
✓ Test on different screen sizes
✓ Maintain visual hierarchy
✓ Keep layouts flat and efficient

### DON'T
✗ Hard-code colors or dimensions
✗ Use arbitrary spacing values
✗ Skip animations entirely
✗ Ignore accessibility
✗ Create deep view hierarchies
✗ Mix design patterns
✗ Forget empty/error states

## 🚀 Next Steps

1. **Explore the app** - Run it and see the redesign
2. **Read the docs** - Understand the design system
3. **Modify something** - Change a color or spacing
4. **Add a feature** - Create a new screen using patterns
5. **Share feedback** - Improve the design system

## 💡 Pro Tips

### Rapid Prototyping
```bash
# Quick rebuild and install
./gradlew installDebug
```

### Live Preview
- Use Android Studio's Layout Editor
- Enable "Show Layout Bounds" in Developer Options
- Use "Layout Inspector" to debug view hierarchy

### Performance
- Use "Profile GPU Rendering" to check animation performance
- Enable "Show overdraw" to optimize layouts
- Monitor memory usage with Android Profiler

## 📞 Need Help?

- Check documentation files in project root
- Review existing layouts for patterns
- Examine UIHelper.java for animation examples
- Look at styles.xml for component customization

---

**Happy Coding! 🎨**
