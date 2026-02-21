package com.github.actions.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

/**
 * Centralized theme and styling helper for consistent UI across the application.
 * Provides methods to apply themes, colors, and styles to views.
 */
public class ThemeHelper {
    private static final String THEME_PREFS = "GitCodeTheme";
    private static final String KEY_DARK_MODE = "darkMode";
    private static boolean cachedIsDark = true;

    /**
     * Check if dark mode is currently enabled
     */
    public static boolean isDarkMode(Context context) {
        return cachedIsDark;
    }

    /**
     * Update the cached dark mode state
     */
    public static void updateDarkModeCache(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE);
        cachedIsDark = prefs.getBoolean(KEY_DARK_MODE, true);
    }

    /**
     * Apply theme to activity window (status bar, navigation bar)
     */
    public static void applyThemeToWindow(Window window, boolean isDark) {
        if (window == null) return;

        // Status bar color
        int statusBarColor = isDark ? 0xFF1E1E1E : 0xFFFFFFFF;
        window.setStatusBarColor(statusBarColor);

        // Navigation bar color
        int navBarColor = isDark ? 0xFF121212 : 0xFFFAFAFA;
        window.setNavigationBarColor(navBarColor);

        // Light status bar flags
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int decorView = window.getDecorView();
            int flags = decorView.getSystemUiVisibility();
            if (isDark) {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            decorView.setSystemUiVisibility(flags);
        }

        // Light navigation bar flags (API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int decorView = window.getDecorView();
            int flags = decorView.getSystemUiVisibility();
            if (isDark) {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            } else {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
            decorView.setSystemUiVisibility(flags);
        }
    }

    /**
     * Apply dark theme colors to a view hierarchy
     */
    public static void applyDarkThemeToView(View view, boolean isDark) {
        if (view instanceof EditText) {
            EditText editText = (EditText) view;
            if (isDark) {
                editText.setTextColor(0xFFE0E0E0);
                editText.setHintTextColor(0xFF757575);
            } else {
                editText.setTextColor(0xFF1F2937);
                editText.setHintTextColor(0xFF9E9E9E);
            }
        } else if (view instanceof Button) {
            Button button = (Button) view;
            if (isDark) {
                button.setTextColor(0xFFFFFFFF);
            } else {
                button.setTextColor(0xFF000000);
            }
        } else if (view instanceof TextView) {
            TextView textView = (TextView) view;
            if (isDark) {
                textView.setTextColor(0xFFE0E0E0);
            } else {
                textView.setTextColor(0xFF1F2937);
            }
        }

        // Recursively apply to children
        if (view instanceof LinearLayout) {
            LinearLayout layout = (LinearLayout) view;
            for (int i = 0; i < layout.getChildCount(); i++) {
                applyDarkThemeToView(layout.getChildAt(i), isDark);
            }
        }
    }

    /**
     * Create a themed AlertDialog with proper dark mode support
     */
    public static class ThemedDialog {
        private final AlertDialog.Builder builder;
        private final Context context;
        private final boolean isDark;

        public ThemedDialog(Context context) {
            this.context = context;
            this.isDark = isDarkMode(context);
            this.builder = new AlertDialog.Builder(context, isDark ? 
                android.R.style.Theme_Material_Dialog_Alert : 
                android.R.style.Theme_Material_Light_Dialog_Alert);
        }

        public ThemedDialog setTitle(String title) {
            builder.setTitle(title);
            return this;
        }

        public ThemedDialog setMessage(String message) {
            builder.setMessage(message);
            return this;
        }

        public ThemedDialog setView(View view) {
            builder.setView(view);
            return this;
        }

        public ThemedDialog setPositiveButton(String text, android.content.DialogInterface.OnClickListener listener) {
            builder.setPositiveButton(text, listener);
            return this;
        }

        public ThemedDialog setNegativeButton(String text, android.content.DialogInterface.OnClickListener listener) {
            builder.setNegativeButton(text, listener);
            return this;
        }

        public ThemedDialog setNeutralButton(String text, android.content.DialogInterface.OnClickListener listener) {
            builder.setNeutralButton(text, listener);
            return this;
        }

        public AlertDialog show() {
            AlertDialog dialog = builder.create();

            // Apply dark theme before showing
            if (isDark && dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0xFF2D2D2D));
            }

            dialog.show();

            // Apply button colors after showing
            if (isDark && dialog.getWindow() != null) {
                new android.os.Handler().post(() -> {
                    try {
                        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                        Button neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);

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
    }

    /**
     * Create a themed input field
     */
    public static EditText createStyledEditText(Context context, String hint) {
        EditText editText = new EditText(context);
        editText.setHint(hint);
        boolean isDark = isDarkMode(context);
        
        if (isDark) {
            editText.setTextColor(0xFFE0E0E0);
            editText.setHintTextColor(0xFF757575);
            editText.setBackgroundColor(0xFF252525);
        } else {
            editText.setTextColor(0xFF1F2937);
            editText.setHintTextColor(0xFF9E9E9E);
            editText.setBackgroundColor(0xFFF9FAFB);
        }
        
        editText.setPadding(16, 16, 16, 16);
        editText.setTextSize(16);
        
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(8);
        drawable.setStroke(1, isDark ? 0xFF404040 : 0xFFD1D5DB);
        editText.setBackground(drawable);
        
        return editText;
    }

    /**
     * Create a styled button
     */
    public static Button createStyledButton(Context context, String text, boolean isPrimary) {
        Button button = new Button(context);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextSize(14);
        button.setPadding(24, 12, 24, 12);
        
        boolean isDark = isDarkMode(context);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(8);
        
        if (isPrimary) {
            drawable.setColor(isDark ? 0xFF4CAF50 : 0xFF2E7D32);
            button.setTextColor(0xFFFFFFFF);
        } else {
            drawable.setColor(Color.TRANSPARENT);
            drawable.setStroke(1, isDark ? 0xFF4CAF50 : 0xFF2E7D32);
            button.setTextColor(isDark ? 0xFF4CAF50 : 0xFF2E7D32);
        }
        
        button.setBackground(drawable);
        button.setMinHeight(48);
        
        return button;
    }

    /**
     * Get color resource based on theme
     */
    public static int getColor(Context context, String colorName) {
        boolean isDark = isDarkMode(context);
        return context.getResources().getIdentifier(
            (isDark ? "dark_" : "light_") + colorName,
            "color",
            context.getPackageName()
        );
    }

    /**
     * Toggle dark mode and restart activity
     */
    public static void toggleDarkMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE);
        boolean currentDark = prefs.getBoolean(KEY_DARK_MODE, true);
        prefs.edit().putBoolean(KEY_DARK_MODE, !currentDark).apply();
        updateDarkModeCache(context);
    }

    /**
     * Get editor colors based on theme
     */
    public static class EditorColors {
        public int background;
        public int text;
        public int lineNumbersBg;
        public int lineNumbersText;
        public int selection;
        public int bracketHighlight;
        public int divider;

        public EditorColors(boolean isDark) {
            if (isDark) {
                background = 0xFF1A1A1A;
                text = 0xFFE8E8E8;
                lineNumbersBg = 0xFF252525;
                lineNumbersText = 0xFF6B7280;
                selection = 0x6633B5E5;
                bracketHighlight = 0x66FFD700;
                divider = 0xFF333333;
            } else {
                background = 0xFFFFFFFF;
                text = 0xFF1F2937;
                lineNumbersBg = 0xFFF5F5F5;
                lineNumbersText = 0xFF9CA3AF;
                selection = 0x802196F3;
                bracketHighlight = 0x80FFD700;
                divider = 0xFFE5E7EB;
            }
        }
    }
}
