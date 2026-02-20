# GitCode Design System Guide

## 🎨 Color Palette

### Primary Colors
```
Primary:         #00C853  ████████  Vibrant Green - Actions, CTAs
Primary Variant: #00E676  ████████  Lighter Green - Hover states
Primary Dark:    #00A843  ████████  Darker Green - Pressed states
```

### Surface Colors (Dark Theme)
```
Surface:         #0D1117  ████████  Base background
Surface Elevated:#161B22  ████████  Cards, elevated elements
Surface Overlay: #1C2128  ████████  Overlays, modals
```

### Text Colors
```
Text Primary:    #E6EDF3  ████████  Main content
Text Secondary:  #8B949E  ████████  Supporting text
Text Tertiary:   #6E7681  ████████  Hints, captions
```

### Borders & Dividers
```
Border:          #30363D  ████████  Component borders
Divider:         #21262D  ████████  Section dividers
```

### Semantic Colors
```
Success:         #3FB950  ████████  Success states
Warning:         #D29922  ████████  Warning states
Error:           #F85149  ████████  Error states
Info:            #58A6FF  ████████  Info states
```

### Editor Colors
```
Editor BG:       #0D1117  ████████  Editor background
Editor Gutter:   #161B22  ████████  Line numbers area
Line Highlight:  #1C2128  ████████  Current line
Selection:       #264F78  ████████  Text selection
```

### Syntax Highlighting
```
Keyword:         #FF7B72  ████████  Keywords (if, for, class)
String:          #A5D6FF  ████████  String literals
Comment:         #8B949E  ████████  Comments
Function:        #D2A8FF  ████████  Function names
Number:          #79C0FF  ████████  Numeric values
```

## 📏 Spacing Scale

```
XS:   4dp   ▌        Tight spacing between related items
SM:   8dp   ▌▌       Base unit, small gaps
MD:  16dp   ▌▌▌▌     Standard padding, default spacing
LG:  24dp   ▌▌▌▌▌▌   Section spacing, larger gaps
XL:  32dp   ▌▌▌▌▌▌▌▌ Large gaps between major sections
XXL: 48dp   ▌▌▌▌▌▌▌▌▌▌▌▌ Major section breaks
```

### Usage Examples
```
Card padding:           16dp (MD)
Button padding:         24dp (LG) horizontal
Screen padding:         16dp (MD)
Section spacing:        32dp (XL)
Header bottom margin:   48dp (XXL)
Icon margin:            8dp (SM)
```

## 🔤 Typography Scale

### Display (32sp)
```
Usage: Main headings, branding
Weight: Bold (sans-serif-medium)
Letter Spacing: -0.02
Example: "GitCode"
```

### Headline (24sp)
```
Usage: Section headers
Weight: Semi-bold (sans-serif-medium)
Letter Spacing: -0.01
Example: "Recent Projects"
```

### Title (20sp)
```
Usage: Card titles, dialog titles
Weight: Medium (sans-serif-medium)
Letter Spacing: 0
Example: "GitHub Credentials"
```

### Body (14sp)
```
Usage: Main content, descriptions
Weight: Regular (sans-serif)
Line Height: 1.5x
Example: "Select a file from the sidebar"
```

### Caption (12sp)
```
Usage: Hints, metadata, timestamps
Weight: Regular (sans-serif)
Example: "Modified 2 hours ago"
```

### Editor (14sp)
```
Usage: Code editor
Font: Monospace
Line Height: +4dp
Example: "function hello() { }"
```

## 🎯 Component Dimensions

### Buttons
```
Height:         48dp
Padding:        24dp horizontal
Corner Radius:  8dp
Text Size:      14sp
Min Width:      64dp
```

### Input Fields
```
Height:         56dp
Corner Radius:  8dp
Padding:        16dp
Text Size:      14sp
Stroke Width:   1dp
```

### Cards
```
Corner Radius:  12dp
Padding:        16dp
Stroke Width:   1dp
Elevation:      0dp (flat design)
```

### Tabs
```
Height:         48dp
Min Width:      120dp
Max Width:      200dp
Padding:        16dp horizontal
```

### Touch Targets
```
Minimum:        48dp × 48dp
Icon Size:      24dp
Small Icon:     20dp
```

## 🎭 Animation Timing

### Durations
```
Short:    150ms  ▌▌▌       Quick feedback, button press
Medium:   250ms  ▌▌▌▌▌     Standard transitions, fades
Long:     350ms  ▌▌▌▌▌▌▌   Complex animations, slides
```

### Easing Functions
```
DecelerateInterpolator:  Natural slowdown (default)
OvershootInterpolator:   Playful bounce effect
LinearInterpolator:      Constant speed (rare)
```

### Animation Types
```
Fade In:        0 → 1 alpha, 250ms
Fade Out:       1 → 0 alpha, 150ms
Slide Up:       translateY, 250ms
Scale Press:    0.95 scale, 100ms
Pulse:          1.05 scale, 200ms
```

## 🎨 Usage Guidelines

### Color Usage
```
✓ DO:
  - Use primary color for CTAs
  - Use semantic colors for states
  - Maintain text contrast ratios
  - Use surface hierarchy for depth

✗ DON'T:
  - Mix light and dark themes
  - Use primary for large areas
  - Ignore accessibility contrast
  - Overuse bright colors
```

### Spacing Usage
```
✓ DO:
  - Follow 8dp grid
  - Use consistent padding
  - Group related items
  - Create breathing room

✗ DON'T:
  - Use arbitrary values
  - Crowd elements
  - Mix spacing scales
  - Ignore visual rhythm
```

### Typography Usage
```
✓ DO:
  - Use scale consistently
  - Maintain hierarchy
  - Optimize line height
  - Consider readability

✗ DON'T:
  - Mix too many sizes
  - Use tiny text (<12sp)
  - Ignore line spacing
  - Overcrowd text
```

### Animation Usage
```
✓ DO:
  - Animate with purpose
  - Use consistent timing
  - Provide feedback
  - Keep it subtle

✗ DON'T:
  - Animate everything
  - Use slow animations
  - Block interactions
  - Overuse effects
```

## 📱 Responsive Behavior

### Phone (< 600dp)
```
- Single column layouts
- Full-width cards
- Stacked buttons
- Drawer navigation
```

### Tablet (≥ 600dp)
```
- Two column layouts
- Max-width cards (600dp)
- Side-by-side buttons
- Persistent navigation
```

## 🔧 Implementation

### Using Colors
```xml
android:textColor="@color/text_primary_dark"
android:background="@color/surface_elevated_dark"
```

### Using Dimensions
```xml
android:padding="@dimen/spacing_md"
android:layout_marginBottom="@dimen/spacing_lg"
```

### Using Styles
```xml
style="@style/Text.Title"
style="@style/Button.Primary"
```

### Using Animations
```java
UIHelper.fadeIn(view);
UIHelper.scalePress(button);
```

## 🎯 Design Checklist

Before shipping any screen:
- [ ] Uses design system colors
- [ ] Follows 8dp spacing grid
- [ ] Implements typography scale
- [ ] Has proper touch targets (48dp)
- [ ] Includes loading states
- [ ] Handles empty states
- [ ] Shows error states
- [ ] Has smooth animations
- [ ] Maintains visual hierarchy
- [ ] Feels intentional and polished

---

**Design System Version: 1.0**
**Last Updated: 2026-02-20**
