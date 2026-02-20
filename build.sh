#!/bin/bash
# Build script for GitHub Actions Android App

echo "GitHub Actions Android App - Build Script"
echo "=========================================="

# Check if ANDROID_HOME is set
if [ -z "$ANDROID_HOME" ]; then
    echo "Error: ANDROID_HOME is not set"
    echo "Please set ANDROID_HOME to your Android SDK location"
    echo "Example: export ANDROID_HOME=/path/to/android-sdk"
    exit 1
fi

# Check if Android SDK exists
if [ ! -d "$ANDROID_HOME" ]; then
    echo "Error: Android SDK not found at $ANDROID_HOME"
    exit 1
fi

echo "Android SDK found at: $ANDROID_HOME"

# Create local.properties
echo "sdk.dir=$ANDROID_HOME" > local.properties
echo "Created local.properties"

# Make gradlew executable
chmod +x gradlew

# Clean previous builds
echo "Cleaning previous builds..."
./gradlew clean

# Build release APK
echo "Building release APK..."
./gradlew assembleRelease

# Check if build was successful
if [ $? -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo "Build successful!"
    echo "APK location: app/build/outputs/apk/release/app-release.apk"
    echo "=========================================="
    ls -lh app/build/outputs/apk/release/app-release.apk 2>/dev/null || echo "APK file not found"
else
    echo ""
    echo "=========================================="
    echo "Build failed!"
    echo "=========================================="
    exit 1
fi
