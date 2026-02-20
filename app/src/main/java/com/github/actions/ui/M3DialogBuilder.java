package com.github.actions.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.content.DialogInterface;

/**
 * Material 3 Dialog Builder
 * Drop-in replacement for AlertDialog.Builder with Material 3 styling
 */
public class M3DialogBuilder {
    
    private final AlertDialog.Builder builder;
    private final Context context;
    private final boolean isDark;
    
    // Material 3 Colors
    private static final int M3_SURFACE_DARK = 0xFF1C1B1F;
    private static final int M3_SURFACE_LIGHT = 0xFFFEFBFF;
    private static final int M3_PRIMARY_DARK = 0xFFD0BCFF;
    private static final int M3_PRIMARY_LIGHT = 0xFF6750A4;
    
    public M3DialogBuilder(Context context) {
        this.context = context;
        this.builder = new AlertDialog.Builder(context);
        
        SharedPreferences prefs = context.getSharedPreferences("GitCodeTheme", Context.MODE_PRIVATE);
        this.isDark = prefs.getBoolean("darkMode", true);
    }
    
    public M3DialogBuilder setTitle(CharSequence title) {
        builder.setTitle(title);
        return this;
    }
    
    public M3DialogBuilder setMessage(CharSequence message) {
        builder.setMessage(message);
        return this;
    }
    
    public M3DialogBuilder setView(View view) {
        builder.setView(view);
        return this;
    }
    
    public M3DialogBuilder setPositiveButton(CharSequence text, DialogInterface.OnClickListener listener) {
        builder.setPositiveButton(text, listener);
        return this;
    }
    
    public M3DialogBuilder setNegativeButton(CharSequence text, DialogInterface.OnClickListener listener) {
        builder.setNegativeButton(text, listener);
        return this;
    }
    
    public M3DialogBuilder setNeutralButton(CharSequence text, DialogInterface.OnClickListener listener) {
        builder.setNeutralButton(text, listener);
        return this;
    }
    
    public M3DialogBuilder setItems(CharSequence[] items, DialogInterface.OnClickListener listener) {
        builder.setItems(items, listener);
        return this;
    }
    
    public AlertDialog create() {
        AlertDialog dialog = builder.create();
        applyMaterial3Styling(dialog);
        return dialog;
    }
    
    public AlertDialog show() {
        AlertDialog dialog = create();
        dialog.show();
        return dialog;
    }
    
    private void applyMaterial3Styling(AlertDialog dialog) {
        if (dialog.getWindow() != null) {
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(isDark ? M3_SURFACE_DARK : M3_SURFACE_LIGHT);
            bg.setCornerRadius(28 * context.getResources().getDisplayMetrics().density);
            dialog.getWindow().setBackgroundDrawable(bg);
            dialog.getWindow().setElevation(24 * context.getResources().getDisplayMetrics().density);
        }
    }
}
