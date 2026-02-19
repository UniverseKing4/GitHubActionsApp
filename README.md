# GitCode - Professional Mobile IDE for Android

<div align="center">

**A fully-featured, professional mobile IDE for Android with GitHub integration, advanced code editing, and modern development tools.**

[![Latest Release](https://img.shields.io/github/v/release/UniverseKing4/GitHubActionsApp)](https://github.com/UniverseKing4/GitHubActionsApp/releases)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/platform-Android-green.svg)](https://www.android.com/)

[Download APK](https://github.com/UniverseKing4/GitHubActionsApp/releases/latest) • [Report Bug](https://github.com/UniverseKing4/GitHubActionsApp/issues) • [Request Feature](https://github.com/UniverseKing4/GitHubActionsApp/issues)

</div>

---

## 🌟 Overview

GitCode transforms your Android device into a powerful development environment. Write, edit, and manage code projects with professional-grade features including syntax highlighting, GitHub integration, and intelligent code editing tools.

## ✨ Key Features

### 🎨 **Advanced Code Editor**
- **Multi-Language Syntax Highlighting** - Support for Java, JavaScript, Python, C/C++, Go, Rust, PHP, Ruby, Swift, Kotlin, HTML, CSS, and more
- **Smart Line Numbers** - Dynamic, perfectly aligned line numbers that scale with your code
- **Bracket Matching** - Click any bracket to highlight its matching pair
- **Auto-Indent & Auto-Brackets** - Intelligent code formatting as you type
- **Customizable Font Size** - Adjustable from 10sp to 30sp for comfortable coding
- **Word Wrap** - Toggle word wrapping for long lines
- **Undo/Redo** - Full undo/redo support with keyboard shortcuts

### 📑 **Tabbed Interface**
- Open multiple files simultaneously
- Quick tab switching
- Individual close buttons on each tab
- Active tab highlighting
- Seamless file navigation

### 🔗 **GitHub Integration**
- **Clone Repositories** - Clone any public/private GitHub repository via URL
- **Smart Push** - Automatically detects and pushes only modified files
- **Pull Changes** - Sync your local project with remote repository
- **File Deletion Detection** - Automatically removes deleted files from GitHub
- **Multiple Profiles** - Switch between different GitHub accounts
- **Secure Authentication** - Personal Access Token support

### 📁 **Project Management**
- Create and manage multiple projects
- Organize files and folders with intuitive UI
- Rename, delete, and move files/folders
- File selection mode with batch operations
- Project statistics (file count, folder count, lines of code, character count)
- Recent projects list on home screen

### 🎯 **Developer Tools**
- **Find & Replace** - Search and replace text across files
- **Auto-Save** - Never lose your work
- **Keyboard Shortcuts** - Efficient coding with shortcuts
- **File Browser** - Expandable/collapsible folder tree
- **Welcome Messages** - Helpful prompts when no files are open

### 🌙 **Modern UI/UX**
- **Dark Mode** - Eye-friendly dark theme (default)
- **Light Mode** - Clean light theme option
- **Compact Toolbar** - Efficient use of screen space
- **Responsive Design** - Optimized for all screen sizes
- **Smooth Animations** - Polished user experience

## 📸 Screenshots

*(Coming soon)*

## 🚀 Getting Started

### Installation

#### Option 1: Download Pre-built APK (Recommended)
1. Go to [Releases](https://github.com/UniverseKing4/GitHubActionsApp/releases/latest)
2. Download the latest `app-release-signed.apk`
3. Install on your Android device
4. Grant necessary permissions when prompted

#### Option 2: Build from Source
```bash
# Clone the repository
git clone https://github.com/UniverseKing4/GitHubActionsApp.git
cd GitHubActionsApp

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
- **UI Framework**: Native Android Views
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
GitHubActionsApp/
├── app/src/main/
│   ├── java/com/github/actions/
│   │   ├── ProjectsActivity.java    # Home screen & project management
│   │   ├── IDEActivity.java         # Main code editor
│   │   └── GitHubAPI.java           # GitHub integration
│   ├── res/
│   │   ├── drawable/                # Icons and graphics
│   │   ├── mipmap-*/                # Launcher icons
│   │   └── layout/                  # XML layouts
│   └── AndroidManifest.xml
├── .github/workflows/
│   └── android.yml                  # CI/CD pipeline
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

### Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 🐛 Known Issues

- Large files (>5MB) may cause performance issues
- Binary files are not supported for editing
- GitHub API rate limits apply (60 requests/hour unauthenticated, 5000/hour authenticated)

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Built with ❤️ for mobile developers
- Inspired by professional desktop IDEs
- Thanks to the Android and GitHub communities

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/UniverseKing4/GitHubActionsApp/issues)
- **Discussions**: [GitHub Discussions](https://github.com/UniverseKing4/GitHubActionsApp/discussions)

---

<div align="center">

**Made with ❤️ for developers who code on the go**

⭐ Star this repo if you find it useful!

</div>
