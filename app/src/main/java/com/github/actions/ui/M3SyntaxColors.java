package com.github.actions.ui;

/**
 * Material 3 Syntax Highlighting Colors
 * Professional, accessible color schemes for code editor
 */
public class M3SyntaxColors {
    
    // Dark Theme Colors (optimized for low-light coding)
    public static class Dark {
        public static final int KEYWORD = 0xFFC792EA;      // Purple - for, if, while, class
        public static final int STRING = 0xFFC3E88D;       // Green - "text"
        public static final int COMMENT = 0xFF546E7A;      // Gray - // comments
        public static final int NUMBER = 0xFFF78C6C;       // Orange - 123, 0xFF
        public static final int FUNCTION = 0xFF82AAFF;     // Blue - functionName()
        public static final int CLASS = 0xFFFFCB6B;        // Yellow - ClassName
        public static final int OPERATOR = 0xFF89DDFF;     // Cyan - +, -, =, ==
        public static final int VARIABLE = 0xFFEEFFFF;     // White - variableName
        public static final int ANNOTATION = 0xFFC792EA;   // Purple - @Override
        public static final int BRACKET = 0xFF89DDFF;      // Cyan - {}, [], ()
    }
    
    // Light Theme Colors (optimized for bright environments)
    public static class Light {
        public static final int KEYWORD = 0xFF7C4DFF;      // Purple - for, if, while, class
        public static final int STRING = 0xFF388E3C;       // Green - "text"
        public static final int COMMENT = 0xFF90A4AE;      // Gray - // comments
        public static final int NUMBER = 0xFFD84315;       // Orange - 123, 0xFF
        public static final int FUNCTION = 0xFF1976D2;     // Blue - functionName()
        public static final int CLASS = 0xFFF57C00;        // Orange - ClassName
        public static final int OPERATOR = 0xFF00838F;     // Cyan - +, -, =, ==
        public static final int VARIABLE = 0xFF212121;     // Dark Gray - variableName
        public static final int ANNOTATION = 0xFF7C4DFF;   // Purple - @Override
        public static final int BRACKET = 0xFF00838F;      // Cyan - {}, [], ()
    }
}
