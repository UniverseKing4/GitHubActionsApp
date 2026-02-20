package com.github.actions.ui.theme;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Material 3 Design System - Theme Manager
 * Provides dynamic theming with light, dark, and AMOLED modes
 */
public class Theme {
    
    public enum Mode {
        LIGHT, DARK, AMOLED
    }
    
    private final Mode mode;
    private final ColorScheme colors;
    
    public Theme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("GitCodeTheme", Context.MODE_PRIVATE);
        String modeStr = prefs.getString("themeMode", "DARK");
        this.mode = Mode.valueOf(modeStr);
        this.colors = new ColorScheme(mode);
    }
    
    public Mode getMode() {
        return mode;
    }
    
    public ColorScheme getColors() {
        return colors;
    }
    
    public boolean isDark() {
        return mode == Mode.DARK || mode == Mode.AMOLED;
    }
    
    public static class ColorScheme {
        // Surface colors
        public final int surface;
        public final int surfaceVariant;
        public final int surfaceContainer;
        public final int surfaceContainerLow;
        public final int surfaceContainerHigh;
        
        // Primary colors
        public final int primary;
        public final int onPrimary;
        public final int primaryContainer;
        public final int onPrimaryContainer;
        
        // Secondary colors
        public final int secondary;
        public final int onSecondary;
        public final int secondaryContainer;
        public final int onSecondaryContainer;
        
        // Tertiary colors
        public final int tertiary;
        public final int onTertiary;
        public final int tertiaryContainer;
        public final int onTertiaryContainer;
        
        // Error colors
        public final int error;
        public final int onError;
        public final int errorContainer;
        public final int onErrorContainer;
        
        // Background colors
        public final int background;
        public final int onBackground;
        
        // Outline colors
        public final int outline;
        public final int outlineVariant;
        
        // Text colors
        public final int onSurface;
        public final int onSurfaceVariant;
        
        // Editor-specific colors
        public final int editorBackground;
        public final int editorLineNumber;
        public final int editorSelection;
        public final int editorCursor;
        
        public ColorScheme(Mode mode) {
            if (mode == Mode.LIGHT) {
                // Light theme - Material 3 baseline
                surface = 0xFFFEFBFF;
                surfaceVariant = 0xFFE7E0EC;
                surfaceContainer = 0xFFF3EDF7;
                surfaceContainerLow = 0xFFF7F2FA;
                surfaceContainerHigh = 0xFFECE6F0;
                
                primary = 0xFF6750A4;
                onPrimary = 0xFFFFFFFF;
                primaryContainer = 0xFFEADDFF;
                onPrimaryContainer = 0xFF21005D;
                
                secondary = 0xFF625B71;
                onSecondary = 0xFFFFFFFF;
                secondaryContainer = 0xFFE8DEF8;
                onSecondaryContainer = 0xFF1D192B;
                
                tertiary = 0xFF7D5260;
                onTertiary = 0xFFFFFFFF;
                tertiaryContainer = 0xFFFFD8E4;
                onTertiaryContainer = 0xFF31111D;
                
                error = 0xFFB3261E;
                onError = 0xFFFFFFFF;
                errorContainer = 0xFFF9DEDC;
                onErrorContainer = 0xFF410E0B;
                
                background = 0xFFFEFBFF;
                onBackground = 0xFF1C1B1F;
                
                outline = 0xFF79747E;
                outlineVariant = 0xFFCAC4D0;
                
                onSurface = 0xFF1C1B1F;
                onSurfaceVariant = 0xFF49454F;
                
                editorBackground = 0xFFFFFFFF;
                editorLineNumber = 0xFF79747E;
                editorSelection = 0x406750A4;
                editorCursor = 0xFF6750A4;
                
            } else if (mode == Mode.AMOLED) {
                // AMOLED theme - Pure black for battery saving
                surface = 0xFF000000;
                surfaceVariant = 0xFF1A1A1A;
                surfaceContainer = 0xFF0D0D0D;
                surfaceContainerLow = 0xFF050505;
                surfaceContainerHigh = 0xFF1F1F1F;
                
                primary = 0xFFD0BCFF;
                onPrimary = 0xFF381E72;
                primaryContainer = 0xFF4F378B;
                onPrimaryContainer = 0xFFEADDFF;
                
                secondary = 0xFFCCC2DC;
                onSecondary = 0xFF332D41;
                secondaryContainer = 0xFF4A4458;
                onSecondaryContainer = 0xFFE8DEF8;
                
                tertiary = 0xFFEFB8C8;
                onTertiary = 0xFF492532;
                tertiaryContainer = 0xFF633B48;
                onTertiaryContainer = 0xFFFFD8E4;
                
                error = 0xFFF2B8B5;
                onError = 0xFF601410;
                errorContainer = 0xFF8C1D18;
                onErrorContainer = 0xFFF9DEDC;
                
                background = 0xFF000000;
                onBackground = 0xFFE6E1E5;
                
                outline = 0xFF938F99;
                outlineVariant = 0xFF49454F;
                
                onSurface = 0xFFE6E1E5;
                onSurfaceVariant = 0xFFCAC4D0;
                
                editorBackground = 0xFF000000;
                editorLineNumber = 0xFF938F99;
                editorSelection = 0x40D0BCFF;
                editorCursor = 0xFFD0BCFF;
                
            } else {
                // Dark theme - Material 3 baseline
                surface = 0xFF1C1B1F;
                surfaceVariant = 0xFF49454F;
                surfaceContainer = 0xFF211F26;
                surfaceContainerLow = 0xFF1C1B1F;
                surfaceContainerHigh = 0xFF2B2930;
                
                primary = 0xFFD0BCFF;
                onPrimary = 0xFF381E72;
                primaryContainer = 0xFF4F378B;
                onPrimaryContainer = 0xFFEADDFF;
                
                secondary = 0xFFCCC2DC;
                onSecondary = 0xFF332D41;
                secondaryContainer = 0xFF4A4458;
                onSecondaryContainer = 0xFFE8DEF8;
                
                tertiary = 0xFFEFB8C8;
                onTertiary = 0xFF492532;
                tertiaryContainer = 0xFF633B48;
                onTertiaryContainer = 0xFFFFD8E4;
                
                error = 0xFFF2B8B5;
                onError = 0xFF601410;
                errorContainer = 0xFF8C1D18;
                onErrorContainer = 0xFFF9DEDC;
                
                background = 0xFF1C1B1F;
                onBackground = 0xFFE6E1E5;
                
                outline = 0xFF938F99;
                outlineVariant = 0xFF49454F;
                
                onSurface = 0xFFE6E1E5;
                onSurfaceVariant = 0xFFCAC4D0;
                
                editorBackground = 0xFF1C1B1F;
                editorLineNumber = 0xFF938F99;
                editorSelection = 0x40D0BCFF;
                editorCursor = 0xFFD0BCFF;
            }
        }
    }
}
