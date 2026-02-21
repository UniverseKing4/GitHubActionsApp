# GitCode - Professional Mobile IDE for Android

<div align="center">

**A fully-featured, professional mobile IDE for Android with GitHub integration, advanced code editing, large file support, and modern development tools.**

[![Latest Release](https://img.shields.io/github/v/release/UniverseKing4/GitCode)](https://github.com/UniverseKing4/GitCode/releases)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/platform-Android-green.svg)](https://www.android.com/)

[Download APK](https://github.com/UniverseKing4/GitCode/releases/latest) • [Report Bug](https://github.com/UniverseKing4/GitCode/issues) • [Request Feature](https://github.com/UniverseKing4/GitCode/issues)

</div>

---

## 🌟 Overview

GitCode transforms your Android device into a powerful development environment. Write, edit, and manage code projects of any size with professional-grade features including syntax highlighting, intelligent large file handling, GitHub integration, and polished UI/UX.

## ✨ Key Features

### 🎨 **Advanced Code Editor**
- **Multi-Language Syntax Highlighting** - Support for Java, JavaScript, Python, C/C++, Go, Rust, PHP, Ruby, Swift, Kotlin, HTML, CSS, and more
- **Smart Line Numbers** - Dynamic, perfectly aligned line numbers that scale with your code
- **Large File Support** - Intelligent chunking for files >10KB or 1000+ lines with seamless navigation
- **Bracket Matching** - Click any bracket to highlight its matching pair
- **Auto-Indent & Auto-Brackets** - Intelligent code formatting as you type
- **Customizable Font Size** - Adjustable from 10sp to 30sp for comfortable coding
- **Word Wrap** - Toggle word wrapping for long lines
- **Undo/Redo** - Full undo/redo support with visual feedback
- **Go to Line/Part** - Jump to any line or file section instantly

### 📑 **Tabbed Interface**
- Open multiple files simultaneously
- Horizontal scrolling for unlimited tabs
- Quick tab switching with visual indicators
- Individual close buttons on each tab
- Active tab highlighting
- Seamless file navigation

### 🔗 **GitHub Integration**
- **Clone Repositories** - Clone any public/private GitHub repository via URL
- **Smart Push** - Automatically detects and pushes only modified files
- **Pull Changes** - Sync your local project with remote repository
- **File Deletion Detection** - Automatically removes deleted files from GitHub
- **Multiple Profiles** - Switch between different GitHub accounts with ease
- **Secure Authentication** - Personal Access Token support with encrypted storage

### 📁 **Project Management**
- Create and manage multiple projects
- Organize files and folders with intuitive UI
- Rename, delete, and move files/folders
- File selection mode with batch operations
- Project statistics (file count, folder count, lines of code, character count)
- Recent projects list on home screen
- Auto-save functionality

### 🎯 **Developer Tools**
- **Find & Replace** - Search and replace text across files with occurrence tracking
- **Auto-Save** - Never lose your work with automatic saving
- **Keyboard Shortcuts** - Efficient coding with shortcuts
- **File Browser** - Expandable/collapsible folder tree with drawer navigation
- **Welcome Messages** - Helpful prompts when no files are open
- **Toast Notifications** - Clear feedback for all operations

### 🌙 **Premium UI/UX (Redesigned)**
- **Material Design 3** - Modern, professional design system with best-in-class aesthetics
- **Professional Dark Theme** - Optimized for long coding sessions with WCAG AA compliant contrast
- **Clean Light Theme** - Premium alternative with excellent readability
- **Smooth Animations** - 60fps hardware-accelerated transitions and micro-interactions
- **Vector Icons** - Custom icon set designed for clarity and consistency
- **Professional Typography** - Carefully tuned typography scale with 12 text styles
- **Comprehensive Theming** - Semantic color system with 200+ color definitions
- **Accessibility First** - WCAG AA compliant, 48dp minimum touch targets, screen reader support
- **Component Library** - Reusable, consistent UI components throughout the app
- **Responsive Design** - Optimized for phones and tablets with adaptive layouts

## 📸 Screenshots

*(Coming soon)*

## 🚀 Getting Started

### Installation

#### Option 1: Download Pre-built APK (Recommended)
1. Go to [Releases](https://github.com/UniverseKing4/GitCode/releases/latest)
2. Download the latest `app-release-signed.apk`
3. Install on your Android device
4. Grant necessary permissions when prompted

#### Option 2: Build from Source
```bash
# Clone the repository
git clone https://github.com/UniverseKing4/GitCode.git
cd GitCode

# Build with Gradle
./gradlew assembleRelease

# APK will be at: app/build/outputs/apk/release/
```

### First-Time Setup

1. **Launch GitCode** - Open the app after installation
2. **Create a Project** - Tap "New Project" and enter a name
3. **Set Up GitHub** (Optional)
   - Tap "👤 GitHub Profiles"
   - Add your GitHub username and Personal Access Token
   - Save the profile

### Creating a GitHub Personal Access Token

1. Visit [GitHub Settings → Tokens](https://github.com/settings/tokens)
2. Click "Generate new token" → "Generate new token (classic)"
3. Give it a descriptive name (e.g., "GitCode Mobile")
4. Select scopes:
   - ✅ `repo` (Full control of private repositories)
5. Click "Generate token"
6. **Copy the token immediately** (you won't see it again!)
7. Paste it into GitCode when adding a profile

## 📖 Usage Guide

### Creating Your First Project

1. Tap **"New Project"** on the home screen
2. Enter a project name
3. Tap **"Create"**
4. Start coding!

### Working with Files

- **Create File**: Tap "File" → Enter filename with extension
- **Create Folder**: Tap "Folder" → Enter folder name
- **Open File**: Tap any file in the file browser
- **Rename**: Long-press file → Select "Rename"
- **Delete**: Long-press file → Select "Delete"
- **Select Multiple**: Tap "Select" → Check files → Tap "Actions"

### GitHub Workflow

#### Clone a Repository
1. Tap **"Clone Repository"** on home screen
2. Enter full GitHub URL (e.g., `https://github.com/username/repo`)
3. Select your GitHub profile
4. Tap **"Clone"**

#### Push Changes
1. Make changes to your files
2. Tap **💾 Save** (or auto-save handles it)
3. Tap **🚀 Push**
4. Enter commit message
5. Changes are pushed to GitHub

#### Pull Changes
1. Open your project
2. Tap **Menu (☰)** → **"Pull from GitHub"**
3. Select your profile
4. Local files sync with remote repository

### Customizing Settings

1. Tap **"⚙ Settings"** on home screen
2. Adjust **Font Size** with slider (10-30sp)
3. Toggle **Theme** between Dark/Light mode
4. Tap **"Save"** to apply changes

## 🎨 Supported Languages

Syntax highlighting is automatically applied for:

| Language | Extensions |
|----------|-----------|
| Java | `.java` |
| JavaScript | `.js`, `.jsx` |
| TypeScript | `.ts`, `.tsx` |
| Python | `.py` |
| HTML | `.html`, `.htm` |
| CSS | `.css`, `.scss`, `.sass` |
| C/C++ | `.c`, `.cpp`, `.h`, `.hpp` |
| Go | `.go` |
| Rust | `.rs` |
| PHP | `.php` |
| Ruby | `.rb` |
| Swift | `.swift` |
| Kotlin | `.kt` |
| JSON | `.json` |
| YAML | `.yaml`, `.yml` |
| XML | `.xml` |

Plain text files (`.txt`, `.md`, etc.) display without syntax highlighting.

## ⚙️ Technical Details

### Requirements
- **Android Version**: 5.0 (Lollipop) or higher
- **Permissions**:
  - `INTERNET` - GitHub API communication
  - `READ_EXTERNAL_STORAGE` - File access
  - `WRITE_EXTERNAL_STORAGE` - File operations
  - `MANAGE_EXTERNAL_STORAGE` - Project management

### Architecture
- **Language**: Java
- **UI Framework**: Native Android Views with Material Design 3
- **Design System**: Custom comprehensive UI/UX system (see [UI_REDESIGN_DOCUMENTATION.md](UI_REDESIGN_DOCUMENTATION.md))
- **Storage**: SharedPreferences for settings, local filesystem for projects
- **API**: GitHub REST API v3
- **Build System**: Gradle
- **CI/CD**: GitHub Actions (automatic APK builds and releases)

### Security
- Credentials stored locally using Android SharedPreferences
- Personal Access Tokens never logged or transmitted except to GitHub API
- HTTPS-only communication with GitHub
- No third-party analytics or tracking

## 🔧 Development

### Project Structure
```
GitCode/
├── app/src/main/
│   ├── java/com/github/actions/
│   │   ├── ui/                      # UI Helper Classes
│   │   │   ├── ThemeHelper.java     # Theme management
│   │   │   ├── AnimationHelper.java # Animation utilities
│   │   │   └── ComponentBuilder.java # Component factory
│   │   ├── ProjectsActivity.java    # Home screen & project management
│   │   ├── IDEActivity.java         # Main code editor
│   │   ├── EditorActivity.java      # Simple editor
│   │   ├── MainActivity.java        # GitHub integration
│   │   └── GitHubAPI.java           # GitHub API wrapper
│   ├── res/
│   │   ├── drawable/               # Icons and backgrounds (20+)
│   │   ├── layout/                 # XML layouts
│   │   ├── values/                 # Colors, themes, strings
│   │   └── mipmap-*/               # Launcher icons
│   └── AndroidManifest.xml
├── .github/workflows/
│   └── android.yml                 # CI/CD pipeline
├── UI_REDESIGN_DOCUMENTATION.md     # Complete UI/UX design docs
├── DEVELOPER_GUIDE.md               # Developer quick reference
├── CHANGES_SUMMARY.md              # Detailed changes list
├── UI_REDESIGN_VERIFICATION.md     # Implementation checklist
└── README.md
```

### Building Locally

**Prerequisites:**
- Android Studio Arctic Fox or later
- JDK 11 or higher
- Android SDK 30+

**Steps:**
1. Open project in Android Studio
2. Sync Gradle files
3. Build → Build Bundle(s) / APK(s) → Build APK(s)
4. APK location: `app/build/outputs/apk/release/`

### Using the UI/UX Design System

GitCode includes a comprehensive, professional UI/UX design system. For developers:

**Quick Start:**
```java
// Apply theme to activity
ThemeHelper.updateDarkModeCache(this);
ThemeHelper.applyThemeToWindow(getWindow(), ThemeHelper.isDarkMode(this));

// Create styled components
Button button = new ComponentBuilder(this).button()
    .text("Save")
    .style(ButtonStyle.PRIMARY)
    .onClick(v -> save())
    .build();

// Animate views
AnimationHelper.fadeIn(view, AnimationHelper.DURATION_NORMAL);

// Create themed dialogs
new ThemeHelper.ThemedDialog(this)
    .setTitle("Title")
    .setView(inputView)
    .setPositiveButton("OK", listener)
    .show();
```

**Documentation:**
- 📖 [UI/UX Redesign Documentation](UI_REDESIGN_DOCUMENTATION.md) - Complete design system guide
- 🚀 [Developer Guide](DEVELOPER_GUIDE.md) - Quick reference for developers
- 📋 [Changes Summary](CHANGES_SUMMARY.md) - Detailed changes list
- ✅ [Verification Checklist](UI_REDESIGN_VERIFICATION.md) - Implementation verification

### Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 🐛 Known Issues

- GitHub API rate limits apply (60 requests/hour unauthenticated, 5000/hour authenticated)

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Built with ❤️ for mobile developers
- Inspired by professional desktop IDEs
- Thanks to the Android and GitHub communities

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/UniverseKing4/GitCode/issues)
- **Discussions**: [GitHub Discussions](https://github.com/UniverseKing4/GitCode/discussions)

---

<div align="center">

**Made with ❤️ for developers who code on the go**

⭐ Star this repo if you find it useful!

</div>
