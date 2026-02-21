package com.github.actions.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

/**
 * Centralized animation helper for smooth, consistent UI animations
 * and micro-interactions throughout the application.
 */
public class AnimationHelper {

    // Animation durations
    public static final long DURATION_FAST = 150L;
    public static final long DURATION_NORMAL = 300L;
    public static final long DURATION_SLOW = 500L;

    // Interpolators
    private static final AccelerateDecelerateInterpolator ACCELERATE_DECELERATE = 
        new AccelerateDecelerateInterpolator();
    private static final DecelerateInterpolator DECELERATE = new DecelerateInterpolator();

    /**
     * Fade in animation
     */
    public static void fadeIn(View view, long duration) {
        fadeIn(view, duration, null);
    }

    public static void fadeIn(View view, long duration, Runnable onAnimationEnd) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(DECELERATE)
            .withEndAction(onAnimationEnd)
            .start();
    }

    /**
     * Fade out animation
     */
    public static void fadeOut(View view, long duration) {
        fadeOut(view, duration, null);
    }

    public static void fadeOut(View view, long duration, Runnable onAnimationEnd) {
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(ACCELERATE_DECELERATE)
            .withEndAction(() -> {
                view.setVisibility(View.GONE);
                if (onAnimationEnd != null) {
                    onAnimationEnd.run();
                }
            })
            .start();
    }

    /**
     * Scale in animation (spring effect)
     */
    public static void scaleIn(View view, long duration) {
        scaleIn(view, duration, null);
    }

    public static void scaleIn(View view, long duration, Runnable onAnimationEnd) {
        view.setScaleX(0f);
        view.setScaleY(0f);
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);

        AnimatorSet animatorSet = new AnimatorSet();
        
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, 0f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 0f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f);

        animatorSet.playTogether(scaleX, scaleY, alpha);
        animatorSet.setDuration(duration);
        animatorSet.setInterpolator(DECELERATE);
        
        if (onAnimationEnd != null) {
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    onAnimationEnd.run();
                }
            });
        }
        
        animatorSet.start();
    }

    /**
     * Slide up animation
     */
    public static void slideUp(View view, long duration) {
        slideUp(view, duration, null);
    }

    public static void slideUp(View view, long duration, Runnable onAnimationEnd) {
        view.setTranslationY(view.getHeight());
        view.setVisibility(View.VISIBLE);

        view.animate()
            .translationY(0f)
            .setDuration(duration)
            .setInterpolator(DECELERATE)
            .withEndAction(onAnimationEnd)
            .start();
    }

    /**
     * Slide down animation
     */
    public static void slideDown(View view, long duration) {
        slideDown(view, duration, null);
    }

    public static void slideDown(View view, long duration, Runnable onAnimationEnd) {
        view.setTranslationY(-view.getHeight());
        view.setVisibility(View.VISIBLE);

        view.animate()
            .translationY(0f)
            .setDuration(duration)
            .setInterpolator(DECELERATE)
            .withEndAction(onAnimationEnd)
            .start();
    }

    /**
     * Slide in from right
     */
    public static void slideInRight(View view, long duration) {
        slideInRight(view, duration, null);
    }

    public static void slideInRight(View view, long duration, Runnable onAnimationEnd) {
        view.setTranslationX(view.getWidth());
        view.setVisibility(View.VISIBLE);

        view.animate()
            .translationX(0f)
            .setDuration(duration)
            .setInterpolator(DECELERATE)
            .withEndAction(onAnimationEnd)
            .start();
    }

    /**
     * Slide out to right
     */
    public static void slideOutRight(View view, long duration) {
        slideOutRight(view, duration, null);
    }

    public static void slideOutRight(View view, long duration, Runnable onAnimationEnd) {
        view.animate()
            .translationX(view.getWidth())
            .setDuration(duration)
            .setInterpolator(ACCELERATE_DECELERATE)
            .withEndAction(() -> {
                view.setVisibility(View.GONE);
                view.setTranslationX(0f);
                if (onAnimationEnd != null) {
                    onAnimationEnd.run();
                }
            })
            .start();
    }

    /**
     * Pulse animation (for button clicks)
     */
    public static void pulse(View view) {
        pulse(view, DURATION_FAST);
    }

    public static void pulse(View view, long duration) {
        AnimatorSet animatorSet = new AnimatorSet();
        
        ObjectAnimator scaleXDown = ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 0.95f);
        ObjectAnimator scaleYDown = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, 0.95f);
        ObjectAnimator scaleXUp = ObjectAnimator.ofFloat(view, View.SCALE_X, 0.95f, 1f);
        ObjectAnimator scaleYUp = ObjectAnimator.ofFloat(view, View.SCALE_Y, 0.95f, 1f);

        animatorSet.play(scaleXDown).with(scaleYDown);
        animatorSet.play(scaleXUp).with(scaleYUp).after(scaleXDown);
        
        animatorSet.setDuration(duration / 2);
        animatorSet.setInterpolator(ACCELERATE_DECELERATE);
        animatorSet.start();
    }

    /**
     * Shrink animation (for removal)
     */
    public static void shrink(View view, long duration) {
        shrink(view, duration, null);
    }

    public static void shrink(View view, long duration, Runnable onAnimationEnd) {
        view.animate()
            .scaleX(0f)
            .scaleY(0f)
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(ACCELERATE_DECELERATE)
            .withEndAction(() -> {
                view.setVisibility(View.GONE);
                view.setScaleX(1f);
                view.setScaleY(1f);
                view.setAlpha(1f);
                if (onAnimationEnd != null) {
                    onAnimationEnd.run();
                }
            })
            .start();
    }

    /**
     * Expand view height animation
     */
    public static void expandHeight(View view, int targetHeight, long duration) {
        expandHeight(view, targetHeight, duration, null);
    }

    public static void expandHeight(View view, int targetHeight, long duration, Runnable onAnimationEnd) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = 0;
        view.setLayoutParams(params);
        view.setVisibility(View.VISIBLE);

        ValueAnimator animator = ValueAnimator.ofInt(0, targetHeight);
        animator.setDuration(duration);
        animator.setInterpolator(DECELERATE);
        animator.addUpdateListener(animation -> {
            params.height = (int) animation.getAnimatedValue();
            view.setLayoutParams(params);
        });

        if (onAnimationEnd != null) {
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    onAnimationEnd.run();
                }
            });
        }

        animator.start();
    }

    /**
     * Collapse view height animation
     */
    public static void collapseHeight(View view, long duration) {
        collapseHeight(view, duration, null);
    }

    public static void collapseHeight(View view, long duration, Runnable onAnimationEnd) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        int originalHeight = view.getHeight();

        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 0);
        animator.setDuration(duration);
        animator.setInterpolator(ACCELERATE_DECELERATE);
        animator.addUpdateListener(animation -> {
            params.height = (int) animation.getAnimatedValue();
            view.setLayoutParams(params);
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                view.setLayoutParams(params);
                if (onAnimationEnd != null) {
                    onAnimationEnd.run();
                }
            }
        });

        animator.start();
    }

    /**
     * Rotate animation
     */
    public static void rotate(View view, float fromDegrees, float toDegrees, long duration) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, View.ROTATION, fromDegrees, toDegrees);
        animator.setDuration(duration);
        animator.setInterpolator(ACCELERATE_DECELERATE);
        animator.start();
    }

    /**
     * Background color transition
     */
    public static void backgroundColorTransition(View view, int fromColor, int toColor, long duration) {
        ValueAnimator animator = ValueAnimator.ofArgb(fromColor, toColor);
        animator.setDuration(duration);
        animator.setInterpolator(ACCELERATE_DECELERATE);
        animator.addUpdateListener(animation -> {
            view.setBackgroundColor((int) animation.getAnimatedValue());
        });
        animator.start();
    }

    /**
     * Stagger animation for multiple views
     */
    public static void stagger(View[] views, long staggerDelay, AnimationType animationType) {
        stagger(views, staggerDelay, animationType, DURATION_NORMAL);
    }

    public static void stagger(View[] views, long staggerDelay, AnimationType animationType, long animationDuration) {
        for (int i = 0; i < views.length; i++) {
            final View view = views[i];
            view.postDelayed(() -> {
                switch (animationType) {
                    case FADE_IN:
                        fadeIn(view, animationDuration);
                        break;
                    case SCALE_IN:
                        scaleIn(view, animationDuration);
                        break;
                    case SLIDE_UP:
                        slideUp(view, animationDuration);
                        break;
                    case SLIDE_IN_RIGHT:
                        slideInRight(view, animationDuration);
                        break;
                }
            }, i * staggerDelay);
        }
    }

    /**
     * Animation types enum
     */
    public enum AnimationType {
        FADE_IN,
        SCALE_IN,
        SLIDE_UP,
        SLIDE_IN_RIGHT
    }

    /**
     * Ripple effect on view
     */
    public static void rippleEffect(View view) {
        pulse(view, DURATION_FAST);
    }
}
