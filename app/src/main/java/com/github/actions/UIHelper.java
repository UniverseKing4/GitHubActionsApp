package com.github.actions;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

public class UIHelper {
    
    // Animation durations
    public static final int DURATION_SHORT = 150;
    public static final int DURATION_MEDIUM = 250;
    public static final int DURATION_LONG = 350;
    
    /**
     * Fade in animation with optional translation
     */
    public static void fadeIn(View view) {
        fadeIn(view, DURATION_MEDIUM, 0);
    }
    
    public static void fadeIn(View view, int duration, int delay) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .setStartDelay(delay)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }
    
    /**
     * Fade out animation
     */
    public static void fadeOut(View view) {
        fadeOut(view, DURATION_SHORT, null);
    }
    
    public static void fadeOut(View view, int duration, Runnable onComplete) {
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(new DecelerateInterpolator())
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                    if (onComplete != null) onComplete.run();
                }
            })
            .start();
    }
    
    /**
     * Slide up animation
     */
    public static void slideUp(View view) {
        view.setTranslationY(view.getHeight());
        view.setVisibility(View.VISIBLE);
        view.animate()
            .translationY(0)
            .setDuration(DURATION_MEDIUM)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }
    
    /**
     * Scale animation for button press feedback
     */
    public static void scalePress(View view) {
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction(() -> {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start();
            })
            .start();
    }
    
    /**
     * Pulse animation for attention
     */
    public static void pulse(View view) {
        view.animate()
            .scaleX(1.05f)
            .scaleY(1.05f)
            .setDuration(200)
            .setInterpolator(new OvershootInterpolator())
            .withEndAction(() -> {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start();
            })
            .start();
    }
    
    /**
     * Staggered fade in for lists
     */
    public static void staggeredFadeIn(View[] views, int baseDelay) {
        for (int i = 0; i < views.length; i++) {
            fadeIn(views[i], DURATION_MEDIUM, baseDelay + (i * 50));
        }
    }
    
    /**
     * Smooth height animation
     */
    public static void animateHeight(View view, int targetHeight) {
        int initialHeight = view.getHeight();
        ValueAnimator animator = ValueAnimator.ofInt(initialHeight, targetHeight);
        animator.addUpdateListener(animation -> {
            view.getLayoutParams().height = (int) animation.getAnimatedValue();
            view.requestLayout();
        });
        animator.setDuration(DURATION_MEDIUM);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }
    
    /**
     * Convert dp to pixels
     */
    public static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
    
    /**
     * Convert pixels to dp
     */
    public static int pxToDp(Context context, int px) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(px / density);
    }
}
