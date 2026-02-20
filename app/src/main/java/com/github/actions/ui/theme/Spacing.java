package com.github.actions.ui.theme;

import android.content.Context;

/**
 * Material 3 Spacing System
 * Consistent spacing scale for the entire app
 */
public class Spacing {
    
    private final Context context;
    private final float density;
    
    // Base spacing unit (4dp)
    public static final int BASE = 4;
    
    // Spacing scale
    public final int none = 0;
    public final int xxs;   // 2dp
    public final int xs;    // 4dp
    public final int sm;    // 8dp
    public final int md;    // 12dp
    public final int lg;    // 16dp
    public final int xl;    // 24dp
    public final int xxl;   // 32dp
    public final int xxxl;  // 48dp
    
    // Component-specific spacing
    public final int buttonPaddingHorizontal;  // 24dp
    public final int buttonPaddingVertical;    // 10dp
    public final int cardPadding;              // 16dp
    public final int screenPadding;            // 16dp
    public final int listItemPadding;          // 16dp
    public final int iconSize;                 // 24dp
    public final int iconSizeSmall;            // 18dp
    public final int iconSizeLarge;            // 32dp
    
    // Touch targets
    public final int minTouchTarget;           // 48dp
    
    // Elevation (for shadows/depth)
    public final float elevationNone = 0f;
    public final float elevationLow;           // 1dp
    public final float elevationMedium;        // 3dp
    public final float elevationHigh;          // 6dp
    public final float elevationHighest;       // 12dp
    
    public Spacing(Context context) {
        this.context = context;
        this.density = context.getResources().getDisplayMetrics().density;
        
        // Convert dp to px
        xxs = dp(2);
        xs = dp(4);
        sm = dp(8);
        md = dp(12);
        lg = dp(16);
        xl = dp(24);
        xxl = dp(32);
        xxxl = dp(48);
        
        buttonPaddingHorizontal = dp(24);
        buttonPaddingVertical = dp(10);
        cardPadding = dp(16);
        screenPadding = dp(16);
        listItemPadding = dp(16);
        iconSize = dp(24);
        iconSizeSmall = dp(18);
        iconSizeLarge = dp(32);
        
        minTouchTarget = dp(48);
        
        elevationLow = 1f * density;
        elevationMedium = 3f * density;
        elevationHigh = 6f * density;
        elevationHighest = 12f * density;
    }
    
    /**
     * Convert dp to pixels
     */
    public int dp(int dp) {
        return (int) (dp * density);
    }
    
    /**
     * Convert sp to pixels
     */
    public int sp(int sp) {
        return (int) (sp * context.getResources().getDisplayMetrics().scaledDensity);
    }
}
