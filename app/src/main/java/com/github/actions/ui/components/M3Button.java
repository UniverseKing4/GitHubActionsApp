package com.github.actions.ui.components;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import com.github.actions.ui.theme.DesignSystem;

/**
 * Material 3 Button Component
 * Provides filled, outlined, and text button variants
 */
public class M3Button extends androidx.appcompat.widget.AppCompatButton {
    
    public enum Style {
        FILLED,      // Primary action
        TONAL,       // Secondary action
        OUTLINED,    // Medium emphasis
        TEXT         // Low emphasis
    }
    
    private final DesignSystem ds;
    private Style style = Style.FILLED;
    
    public M3Button(Context context) {
        super(context);
        ds = DesignSystem.getInstance(context);
        init();
    }
    
    public M3Button(Context context, Style style) {
        super(context);
        ds = DesignSystem.getInstance(context);
        this.style = style;
        init();
    }
    
    private void init() {
        // Typography
        setTextSize(TypedValue.COMPLEX_UNIT_SP, ds.typography.labelLarge.size);
        setTypeface(ds.typography.labelLarge.typeface);
        setAllCaps(false);
        
        // Padding
        setPadding(
            ds.spacing.buttonPaddingHorizontal,
            ds.spacing.buttonPaddingVertical,
            ds.spacing.buttonPaddingHorizontal,
            ds.spacing.buttonPaddingVertical
        );
        
        // Min height
        setMinHeight(ds.spacing.minTouchTarget);
        setGravity(Gravity.CENTER);
        
        // Apply style
        applyStyle();
    }
    
    public void setStyle(Style style) {
        this.style = style;
        applyStyle();
    }
    
    private void applyStyle() {
        switch (style) {
            case FILLED:
                applyFilledStyle();
                break;
            case TONAL:
                applyTonalStyle();
                break;
            case OUTLINED:
                applyOutlinedStyle();
                break;
            case TEXT:
                applyTextStyle();
                break;
        }
    }
    
    private void applyFilledStyle() {
        setTextColor(ds.colors.onPrimary);
        
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(ds.colors.primary);
        bg.setCornerRadius(ds.spacing.dp(20));
        
        GradientDrawable pressed = new GradientDrawable();
        pressed.setColor(adjustAlpha(ds.colors.primary, 0.9f));
        pressed.setCornerRadius(ds.spacing.dp(20));
        
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed}, pressed);
        states.addState(new int[]{}, bg);
        
        setBackground(states);
        setElevation(ds.spacing.elevationLow);
    }
    
    private void applyTonalStyle() {
        setTextColor(ds.colors.onSecondaryContainer);
        
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(ds.colors.secondaryContainer);
        bg.setCornerRadius(ds.spacing.dp(20));
        
        setBackground(bg);
        setElevation(0);
    }
    
    private void applyOutlinedStyle() {
        setTextColor(ds.colors.primary);
        
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(android.graphics.Color.TRANSPARENT);
        bg.setStroke(ds.spacing.dp(1), ds.colors.outline);
        bg.setCornerRadius(ds.spacing.dp(20));
        
        setBackground(bg);
        setElevation(0);
    }
    
    private void applyTextStyle() {
        setTextColor(ds.colors.primary);
        setBackground(null);
        setElevation(0);
        setPadding(ds.spacing.md, ds.spacing.sm, ds.spacing.md, ds.spacing.sm);
    }
    
    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(android.graphics.Color.alpha(color) * factor);
        int red = android.graphics.Color.red(color);
        int green = android.graphics.Color.green(color);
        int blue = android.graphics.Color.blue(color);
        return android.graphics.Color.argb(alpha, red, green, blue);
    }
}
