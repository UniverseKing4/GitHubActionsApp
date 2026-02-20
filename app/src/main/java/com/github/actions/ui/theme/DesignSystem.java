package com.github.actions.ui.theme;

import android.content.Context;

/**
 * Central Design System
 * Provides access to all design tokens
 */
public class DesignSystem {
    
    private static DesignSystem instance;
    
    public final Theme theme;
    public final Typography typography;
    public final Spacing spacing;
    public final Theme.ColorScheme colors;
    
    private DesignSystem(Context context) {
        this.theme = new Theme(context);
        this.typography = new Typography(context);
        this.spacing = new Spacing(context);
        this.colors = theme.getColors();
    }
    
    public static DesignSystem getInstance(Context context) {
        if (instance == null) {
            instance = new DesignSystem(context.getApplicationContext());
        }
        return instance;
    }
    
    public static void reset() {
        instance = null;
    }
    
    /**
     * Quick access to common values
     */
    public boolean isDark() {
        return theme.isDark();
    }
    
    public int getPrimaryColor() {
        return colors.primary;
    }
    
    public int getSurfaceColor() {
        return colors.surface;
    }
    
    public int getBackgroundColor() {
        return colors.background;
    }
    
    public int getOnSurfaceColor() {
        return colors.onSurface;
    }
}
