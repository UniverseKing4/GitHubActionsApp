package com.github.actions.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * UI Enhancement Utility
 * Applies Material 3 styling to existing components without breaking functionality
 */
public class UIEnhancer {
    
    private final Context context;
    private final boolean isDark;
    
    // Material 3 Colors - Dark Theme
    private static final int M3_PRIMARY = 0xFFD0BCFF;
    private static final int M3_ON_PRIMARY = 0xFF381E72;
    private static final int M3_PRIMARY_CONTAINER = 0xFF4F378B;
    private static final int M3_SURFACE = 0xFF1C1B1F;
    private static final int M3_SURFACE_VARIANT = 0xFF49454F;
    private static final int M3_ON_SURFACE = 0xFFE6E1E5;
    private static final int M3_ON_SURFACE_VARIANT = 0xFFCAC4D0;
    private static final int M3_OUTLINE = 0xFF938F99;
    
    // Material 3 Colors - Light Theme
    private static final int M3_PRIMARY_LIGHT = 0xFF6750A4;
    private static final int M3_ON_PRIMARY_LIGHT = 0xFFFFFFFF;
    private static final int M3_SURFACE_LIGHT = 0xFFFEFBFF;
    private static final int M3_ON_SURFACE_LIGHT = 0xFF1C1B1F;
    
    public UIEnhancer(Context context) {
        this.context = context;
        SharedPreferences prefs = context.getSharedPreferences("GitCodeTheme", Context.MODE_PRIVATE);
        this.isDark = prefs.getBoolean("darkMode", true);
    }
    
    /**
     * Enhance a button with Material 3 styling
     */
    public void enhanceButton(Button button, ButtonStyle style) {
        button.setAllCaps(false);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        
        int paddingH = dp(24);
        int paddingV = dp(10);
        button.setPadding(paddingH, paddingV, paddingH, paddingV);
        button.setMinHeight(dp(48));
        
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(20));
        
        switch (style) {
            case FILLED:
                bg.setColor(isDark ? M3_PRIMARY : M3_PRIMARY_LIGHT);
                button.setTextColor(isDark ? M3_ON_PRIMARY : M3_ON_PRIMARY_LIGHT);
                button.setElevation(dp(1));
                break;
            case TONAL:
                bg.setColor(isDark ? M3_PRIMARY_CONTAINER : 0xFFEADDFF);
                button.setTextColor(isDark ? M3_PRIMARY : M3_PRIMARY_LIGHT);
                button.setElevation(0);
                break;
            case OUTLINED:
                bg.setColor(android.graphics.Color.TRANSPARENT);
                bg.setStroke(dp(1), isDark ? M3_OUTLINE : 0xFF79747E);
                button.setTextColor(isDark ? M3_PRIMARY : M3_PRIMARY_LIGHT);
                button.setElevation(0);
                break;
            case TEXT:
                button.setBackground(null);
                button.setTextColor(isDark ? M3_PRIMARY : M3_PRIMARY_LIGHT);
                button.setElevation(0);
                button.setPadding(dp(12), dp(8), dp(12), dp(8));
                return;
        }
        
        button.setBackground(bg);
    }
    
    /**
     * Enhance an EditText with Material 3 styling
     */
    public void enhanceEditText(EditText editText) {
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        editText.setTextColor(isDark ? M3_ON_SURFACE : M3_ON_SURFACE_LIGHT);
        editText.setHintTextColor(isDark ? M3_ON_SURFACE_VARIANT : 0xFF49454F);
        
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(isDark ? M3_SURFACE_VARIANT : 0xFFE7E0EC);
        bg.setCornerRadius(dp(4));
        editText.setBackground(bg);
        
        int padding = dp(16);
        editText.setPadding(padding, padding, padding, padding);
    }
    
    /**
     * Enhance a TextView with Material 3 styling
     */
    public void enhanceTextView(TextView textView, TextStyle style) {
        textView.setTextColor(isDark ? M3_ON_SURFACE : M3_ON_SURFACE_LIGHT);
        
        switch (style) {
            case HEADLINE_LARGE:
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
                break;
            case HEADLINE_MEDIUM:
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
                break;
            case TITLE_LARGE:
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                break;
            case TITLE_MEDIUM:
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                break;
            case BODY_LARGE:
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                break;
            case BODY_MEDIUM:
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                break;
            case LABEL_LARGE:
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                break;
        }
    }
    
    /**
     * Create a Material 3 card background
     */
    public GradientDrawable createCardBackground() {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(isDark ? 0xFF211F26 : 0xFFF3EDF7);
        bg.setCornerRadius(dp(12));
        return bg;
    }
    
    /**
     * Apply Material 3 theme to entire activity
     */
    public void applyToActivity(Activity activity) {
        View rootView = activity.getWindow().getDecorView().getRootView();
        rootView.setBackgroundColor(isDark ? M3_SURFACE : M3_SURFACE_LIGHT);
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(isDark ? 0xFF000000 : M3_SURFACE_LIGHT);
            activity.getWindow().setNavigationBarColor(isDark ? 0xFF000000 : M3_SURFACE_LIGHT);
        }
    }
    
    private int dp(int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }
    
    public enum ButtonStyle {
        FILLED, TONAL, OUTLINED, TEXT
    }
    
    public enum TextStyle {
        HEADLINE_LARGE, HEADLINE_MEDIUM, TITLE_LARGE, TITLE_MEDIUM,
        BODY_LARGE, BODY_MEDIUM, LABEL_LARGE
    }
}
