# GitHub Actions Android App

A feature-rich Android app for committing and pushing files to GitHub using Personal Access Token.

## Features
- 📁 **File Picker** - Select any file from your device to upload
- 💾 **Save Credentials** - Securely save GitHub credentials locally
- ✏️ **Manual Entry** - Type content directly or paste text
- 🚀 **Auto Release** - GitHub Actions automatically builds and releases APK
- 🔒 **Secure** - Token stored locally, never exposed

## New Features
- Select files from device storage
- Save/load GitHub credentials
- Clear saved credentials
- Enhanced UI with sections
- Auto-fill file path from selected file
- Clear form after successful commit

## Building the APK

### Prerequisites
- Android Studio or Android SDK installed
- Java JDK 11 or higher

### Build Instructions

#### Option 1: Using Android Studio
1. Open Android Studio
2. Click "Open an Existing Project"
3. Navigate to the project folder
4. Wait for Gradle sync to complete
5. Click Build > Build Bundle(s) / APK(s) > Build APK(s)
6. APK will be in `app/build/outputs/apk/release/`

#### Option 2: Using Command Line
```bash
./gradlew assembleRelease
```

The APK will be generated at: `app/build/outputs/apk/release/app-release-unsigned.apk`

#### Option 3: Download from Releases
Download the latest APK from the [Releases](https://github.com/UniverseKing4/GitHubActionsApp/releases) page.

## Usage

1. Install the APK on your Android device
2. Open the app
3. Enter your GitHub username
4. Enter your Personal Access Token (create one at https://github.com/settings/tokens)
5. Enter the repository name
6. Check "Save Credentials" to remember your info
7. Either:
   - Tap "Select File from Device" to choose a file, OR
   - Manually enter file path and content
8. Enter a commit message (optional)
9. Tap "Commit & Push"

## Creating a GitHub Personal Access Token

1. Go to https://github.com/settings/tokens
2. Click "Generate new token" > "Generate new token (classic)"
3. Give it a name
4. Select scopes: `repo` (Full control of private repositories)
5. Click "Generate token"
6. Copy the token and use it in the app

## Permissions

- **INTERNET** - Required to communicate with GitHub API
- **READ_EXTERNAL_STORAGE** - Required to select files from device

## GitHub Actions

This repository uses GitHub Actions to automatically:
- Build the release APK on every push
- Create a new release with version number
- Upload the APK to releases

## Note

Credentials are stored locally on your device using SharedPreferences. They are never sent anywhere except directly to GitHub API.
