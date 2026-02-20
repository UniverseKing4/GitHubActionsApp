package com.github.actions.ui.theme;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Material 3 Typography System
 * Defines text styles for the entire app
 */
public class Typography {
    
    // Display styles - Large, prominent text
    public final TextStyle displayLarge;
    public final TextStyle displayMedium;
    public final TextStyle displaySmall;
    
    // Headline styles - Section headers
    public final TextStyle headlineLarge;
    public final TextStyle headlineMedium;
    public final TextStyle headlineSmall;
    
    // Title styles - Emphasized text
    public final TextStyle titleLarge;
    public final TextStyle titleMedium;
    public final TextStyle titleSmall;
    
    // Body styles - Main content
    public final TextStyle bodyLarge;
    public final TextStyle bodyMedium;
    public final TextStyle bodySmall;
    
    // Label styles - Buttons, tabs
    public final TextStyle labelLarge;
    public final TextStyle labelMedium;
    public final TextStyle labelSmall;
    
    // Code styles - Monospace for editor
    public final TextStyle codeLarge;
    public final TextStyle codeMedium;
    public final TextStyle codeSmall;
    
    public Typography(Context context) {
        // Display
        displayLarge = new TextStyle(57, 64, Typeface.DEFAULT, 400);
        displayMedium = new TextStyle(45, 52, Typeface.DEFAULT, 400);
        displaySmall = new TextStyle(36, 44, Typeface.DEFAULT, 400);
        
        // Headline
        headlineLarge = new TextStyle(32, 40, Typeface.DEFAULT, 400);
        headlineMedium = new TextStyle(28, 36, Typeface.DEFAULT, 400);
        headlineSmall = new TextStyle(24, 32, Typeface.DEFAULT, 400);
        
        // Title
        titleLarge = new TextStyle(22, 28, Typeface.DEFAULT, 500);
        titleMedium = new TextStyle(16, 24, Typeface.DEFAULT, 500);
        titleSmall = new TextStyle(14, 20, Typeface.DEFAULT, 500);
        
        // Body
        bodyLarge = new TextStyle(16, 24, Typeface.DEFAULT, 400);
        bodyMedium = new TextStyle(14, 20, Typeface.DEFAULT, 400);
        bodySmall = new TextStyle(12, 16, Typeface.DEFAULT, 400);
        
        // Label
        labelLarge = new TextStyle(14, 20, Typeface.DEFAULT, 500);
        labelMedium = new TextStyle(12, 16, Typeface.DEFAULT, 500);
        labelSmall = new TextStyle(11, 16, Typeface.DEFAULT, 500);
        
        // Code - Monospace
        Typeface monoTypeface = Typeface.MONOSPACE;
        codeLarge = new TextStyle(16, 24, monoTypeface, 400);
        codeMedium = new TextStyle(14, 20, monoTypeface, 400);
        codeSmall = new TextStyle(12, 16, monoTypeface, 400);
    }
    
    public static class TextStyle {
        public final float size;        // sp
        public final float lineHeight;  // sp
        public final Typeface typeface;
        public final int weight;        // 100-900
        
        public TextStyle(float size, float lineHeight, Typeface typeface, int weight) {
            this.size = size;
            this.lineHeight = lineHeight;
            this.typeface = typeface;
            this.weight = weight;
        }
        
        public float getSizePx(Context context) {
            return size * context.getResources().getDisplayMetrics().scaledDensity;
        }
        
        public float getLineHeightPx(Context context) {
            return lineHeight * context.getResources().getDisplayMetrics().scaledDensity;
        }
    }
}
