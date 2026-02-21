package com.github.actions.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

/**
 * Component builder for creating consistent, styled UI elements
 * throughout the application. Provides a fluent API for building
 * views with proper theming and styling.
 */
public class ComponentBuilder {

    private final Context context;
    private final boolean isDark;

    public ComponentBuilder(Context context) {
        this.context = context;
        this.isDark = ThemeHelper.isDarkMode(context);
    }

    // ============================================
    // CARD BUILDER
    // ============================================

    public CardBuilder card() {
        return new CardBuilder();
    }

    public class CardBuilder {
        private int backgroundColor = isDark ? 0xFF252525 : 0xFFFFFFFF;
        private int strokeColor = isDark ? 0xFF404040 : 0xFFD1D5DB;
        private int strokeWidth = 1;
        private float cornerRadius = 12f;
        private int padding = 16;
        private boolean clickable = false;
        private View.OnClickListener onClickListener = null;

        public CardBuilder backgroundColor(int color) {
            this.backgroundColor = color;
            return this;
        }

        public CardBuilder strokeColor(int color) {
            this.strokeColor = color;
            return this;
        }

        public CardBuilder strokeWidth(int width) {
            this.strokeWidth = width;
            return this;
        }

        public CardBuilder cornerRadius(float radius) {
            this.cornerRadius = radius;
            return this;
        }

        public CardBuilder padding(int padding) {
            this.padding = padding;
            return this;
        }

        public CardBuilder clickable(boolean clickable) {
            this.clickable = clickable;
            return this;
        }

        public CardBuilder onClick(View.OnClickListener listener) {
            this.onClickListener = listener;
            this.clickable = true;
            return this;
        }

        public View build() {
            View card = new View(context);

            // Create background drawable
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setColor(backgroundColor);
            drawable.setCornerRadius(cornerRadius);
            if (strokeWidth > 0) {
                drawable.setStroke(strokeWidth, strokeColor);
            }
            card.setBackground(drawable);

            // Set padding
            card.setPadding(padding, padding, padding, padding);

            // Set clickability
            card.setClickable(clickable);
            if (onClickListener != null) {
                card.setOnClickListener(onClickListener);
            }

            return card;
        }

        public LinearLayout buildContainer() {
            LinearLayout container = new LinearLayout(context);
            container.setOrientation(LinearLayout.VERTICAL);
            container.setPadding(padding, padding, padding, padding);

            // Create background drawable
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setColor(backgroundColor);
            drawable.setCornerRadius(cornerRadius);
            if (strokeWidth > 0) {
                drawable.setStroke(strokeWidth, strokeColor);
            }
            container.setBackground(drawable);

            // Set clickability
            container.setClickable(clickable);
            if (onClickListener != null) {
                container.setOnClickListener(onClickListener);
            }

            return container;
        }
    }

    // ============================================
    // BUTTON BUILDER
    // ============================================

    public ButtonBuilder button() {
        return new ButtonBuilder();
    }

    public class ButtonBuilder {
        private String text = "";
        private ButtonStyle style = ButtonStyle.PRIMARY;
        private float cornerRadius = 8f;
        private int paddingStart = 24;
        private int paddingEnd = 24;
        private int paddingTop = 12;
        private int paddingBottom = 12;
        private int minWidth = 0;
        private int minHeight = 48;
        private int textSize = 14;
        private View.OnClickListener onClickListener = null;

        public enum ButtonStyle {
            PRIMARY,
            OUTLINE,
            TEXT
        }

        public ButtonBuilder text(String text) {
            this.text = text;
            return this;
        }

        public ButtonBuilder style(ButtonStyle style) {
            this.style = style;
            return this;
        }

        public ButtonBuilder cornerRadius(float radius) {
            this.cornerRadius = radius;
            return this;
        }

        public ButtonBuilder padding(int horizontal, int vertical) {
            this.paddingStart = this.paddingEnd = horizontal;
            this.paddingTop = this.paddingBottom = vertical;
            return this;
        }

        public ButtonBuilder padding(int start, int top, int end, int bottom) {
            this.paddingStart = start;
            this.paddingTop = top;
            this.paddingEnd = end;
            this.paddingBottom = bottom;
            return this;
        }

        public ButtonBuilder minWidth(int width) {
            this.minWidth = width;
            return this;
        }

        public ButtonBuilder minHeight(int height) {
            this.minHeight = height;
            return this;
        }

        public ButtonBuilder textSize(int size) {
            this.textSize = size;
            return this;
        }

        public ButtonBuilder onClick(View.OnClickListener listener) {
            this.onClickListener = listener;
            return this;
        }

        public Button build() {
            Button button = new Button(context);
            button.setText(text);
            button.setTextSize(textSize);
            button.setAllCaps(false);
            button.setPadding(paddingStart, paddingTop, paddingEnd, paddingBottom);
            button.setMinWidth(minWidth);
            button.setMinHeight(minHeight);

            // Apply style
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(cornerRadius);

            switch (style) {
                case PRIMARY:
                    drawable.setColor(isDark ? 0xFF4CAF50 : 0xFF2E7D32);
                    button.setTextColor(0xFFFFFFFF);
                    break;
                case OUTLINE:
                    drawable.setColor(Color.TRANSPARENT);
                    drawable.setStroke(1, isDark ? 0xFF4CAF50 : 0xFF2E7D32);
                    button.setTextColor(isDark ? 0xFF4CAF50 : 0xFF2E7D32);
                    break;
                case TEXT:
                    drawable.setColor(Color.TRANSPARENT);
                    button.setTextColor(isDark ? 0xFF4CAF50 : 0xFF2E7D32);
                    break;
            }

            button.setBackground(drawable);

            if (onClickListener != null) {
                button.setOnClickListener(onClickListener);
            }

            return button;
        }
    }

    // ============================================
    // TEXT INPUT BUILDER
    // ============================================

    public TextInputBuilder textInput() {
        return new TextInputBuilder();
    }

    public class TextInputBuilder {
        private String hint = "";
        private String text = "";
        private int inputType = android.text.InputType.TYPE_CLASS_TEXT;
        private int textSize = 16;
        private int padding = 16;
        private float cornerRadius = 8f;
        private int strokeColor = isDark ? 0xFF404040 : 0xFFD1D5DB;
        private int strokeFocusedColor = isDark ? 0xFF4CAF50 : 0xFF2E7D32;
        private int strokeWidth = 1;
        private int strokeFocusedWidth = 2;
        private int minHeight = 48;

        public TextInputBuilder hint(String hint) {
            this.hint = hint;
            return this;
        }

        public TextInputBuilder text(String text) {
            this.text = text;
            return this;
        }

        public TextInputBuilder inputType(int inputType) {
            this.inputType = inputType;
            return this;
        }

        public TextInputBuilder textSize(int size) {
            this.textSize = size;
            return this;
        }

        public TextInputBuilder padding(int padding) {
            this.padding = padding;
            return this;
        }

        public TextInputBuilder cornerRadius(float radius) {
            this.cornerRadius = radius;
            return this;
        }

        public TextInputBuilder strokeColor(int color) {
            this.strokeColor = color;
            return this;
        }

        public TextInputBuilder strokeFocusedColor(int color) {
            this.strokeFocusedColor = color;
            return this;
        }

        public TextInputBuilder minHeight(int height) {
            this.minHeight = height;
            return this;
        }

        public EditText build() {
            EditText editText = new EditText(context);
            editText.setHint(hint);
            editText.setText(text);
            editText.setInputType(inputType);
            editText.setTextSize(textSize);
            editText.setPadding(padding, padding, padding, padding);
            editText.setMinHeight(minHeight);

            // Set colors
            if (isDark) {
                editText.setTextColor(0xFFE0E0E0);
                editText.setHintTextColor(0xFF757575);
            } else {
                editText.setTextColor(0xFF1F2937);
                editText.setHintTextColor(0xFF9E9E9E);
            }

            // Create background drawable
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setColor(isDark ? 0xFF252525 : 0xFFF9FAFB);
            drawable.setCornerRadius(cornerRadius);
            drawable.setStroke(strokeWidth, strokeColor);
            editText.setBackground(drawable);

            return editText;
        }
    }

    // ============================================
    // TEXT VIEW BUILDER
    // ============================================

    public TextViewBuilder textView() {
        return new TextViewBuilder();
    }

    public class TextViewBuilder {
        private String text = "";
        private TextType type = TextType.BODY;
        private int textColor = isDark ? 0xFFE0E0E0 : 0xFF1F2937;
        private int paddingStart = 0;
        private int paddingTop = 0;
        private int paddingEnd = 0;
        private int paddingBottom = 0;
        private boolean bold = false;
        private int gravity = Gravity.START;

        public enum TextType {
            HEADLINE_1, HEADLINE_2, HEADLINE_3, HEADLINE_4,
            HEADLINE_5, HEADLINE_6,
            SUBTITLE_1, SUBTITLE_2,
            BODY, CAPTION, OVERLINE
        }

        public TextViewBuilder text(String text) {
            this.text = text;
            return this;
        }

        public TextViewBuilder type(TextType type) {
            this.type = type;
            return this;
        }

        public TextViewBuilder textColor(int color) {
            this.textColor = color;
            return this;
        }

        public TextViewBuilder padding(int padding) {
            this.paddingStart = this.paddingTop = this.paddingEnd = this.paddingBottom = padding;
            return this;
        }

        public TextViewBuilder padding(int start, int top, int end, int bottom) {
            this.paddingStart = start;
            this.paddingTop = top;
            this.paddingEnd = end;
            this.paddingBottom = bottom;
            return this;
        }

        public TextViewBuilder bold(boolean bold) {
            this.bold = bold;
            return this;
        }

        public TextViewBuilder gravity(int gravity) {
            this.gravity = gravity;
            return this;
        }

        public TextView build() {
            TextView textView = new TextView(context);
            textView.setText(text);
            textView.setTextColor(textColor);
            textView.setPadding(paddingStart, paddingTop, paddingEnd, paddingBottom);
            textView.setGravity(gravity);

            // Apply type settings
            switch (type) {
                case HEADLINE_1:
                    textView.setTextSize(96);
                    break;
                case HEADLINE_2:
                    textView.setTextSize(60);
                    break;
                case HEADLINE_3:
                    textView.setTextSize(48);
                    break;
                case HEADLINE_4:
                    textView.setTextSize(34);
                    break;
                case HEADLINE_5:
                    textView.setTextSize(24);
                    break;
                case HEADLINE_6:
                    textView.setTextSize(20);
                    break;
                case SUBTITLE_1:
                    textView.setTextSize(16);
                    break;
                case SUBTITLE_2:
                    textView.setTextSize(14);
                    break;
                case BODY:
                    textView.setTextSize(16);
                    break;
                case CAPTION:
                    textView.setTextSize(12);
                    break;
                case OVERLINE:
                    textView.setTextSize(10);
                    break;
            }

            if (bold) {
                textView.setTypeface(null, android.graphics.Typeface.BOLD);
            }

            return textView;
        }
    }

    // ============================================
    // LAYOUT BUILDER
    // ============================================

    public LayoutBuilder layout() {
        return new LayoutBuilder();
    }

    public class LayoutBuilder {
        private int orientation = LinearLayout.VERTICAL;
        private int width = ViewGroup.LayoutParams.MATCH_PARENT;
        private int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        private int padding = 0;
        private int gravity = Gravity.NO_GRAVITY;
        private int backgroundColor = Color.TRANSPARENT;
        private float cornerRadius = 0f;

        public LayoutBuilder orientation(int orientation) {
            this.orientation = orientation;
            return this;
        }

        public LayoutBuilder width(int width) {
            this.width = width;
            return this;
        }

        public LayoutBuilder height(int height) {
            this.height = height;
            return this;
        }

        public LayoutBuilder padding(int padding) {
            this.padding = padding;
            return this;
        }

        public LayoutBuilder gravity(int gravity) {
            this.gravity = gravity;
            return this;
        }

        public LayoutBuilder backgroundColor(int color) {
            this.backgroundColor = color;
            return this;
        }

        public LayoutBuilder cornerRadius(float radius) {
            this.cornerRadius = radius;
            return this;
        }

        public LinearLayout build() {
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(orientation);
            layout.setLayoutParams(new LinearLayout.LayoutParams(width, height));
            layout.setPadding(padding, padding, padding, padding);
            layout.setGravity(gravity);

            if (backgroundColor != Color.TRANSPARENT || cornerRadius > 0) {
                GradientDrawable drawable = new GradientDrawable();
                drawable.setShape(GradientDrawable.RECTANGLE);
                drawable.setColor(backgroundColor);
                drawable.setCornerRadius(cornerRadius);
                layout.setBackground(drawable);
            }

            return layout;
        }
    }

    // ============================================
    // DIVIDER BUILDER
    // ============================================

    public View divider() {
        return divider(ViewGroup.LayoutParams.MATCH_PARENT, 1);
    }

    public View divider(int width, int height) {
        View divider = new View(context);
        divider.setLayoutParams(new LinearLayout.LayoutParams(width, height));
        divider.setBackgroundColor(isDark ? 0xFF333333 : 0xFFE5E7EB);
        return divider;
    }

    // ============================================
    // SPACE BUILDER
    // ============================================

    public View space(int width, int height) {
        View space = new View(context);
        space.setLayoutParams(new LinearLayout.LayoutParams(width, height));
        return space;
    }
}
