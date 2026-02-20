package com.github.actions.ui.components;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.actions.ui.theme.DesignSystem;

/**
 * Material 3 Card Component
 * Elevated surface for grouping content
 */
public class M3Card extends LinearLayout {
    
    public enum Elevation {
        NONE, LOW, MEDIUM, HIGH
    }
    
    private final DesignSystem ds;
    
    public M3Card(Context context) {
        super(context);
        ds = DesignSystem.getInstance(context);
        init();
    }
    
    private void init() {
        setOrientation(VERTICAL);
        setPadding(ds.spacing.cardPadding, ds.spacing.cardPadding, 
                   ds.spacing.cardPadding, ds.spacing.cardPadding);
        
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(ds.colors.surfaceContainer);
        bg.setCornerRadius(ds.spacing.dp(12));
        setBackground(bg);
        
        setElevation(ds.spacing.elevationLow);
    }
    
    public void setCardElevation(Elevation elevation) {
        switch (elevation) {
            case NONE:
                setElevation(ds.spacing.elevationNone);
                break;
            case LOW:
                setElevation(ds.spacing.elevationLow);
                break;
            case MEDIUM:
                setElevation(ds.spacing.elevationMedium);
                break;
            case HIGH:
                setElevation(ds.spacing.elevationHigh);
                break;
        }
    }
}
