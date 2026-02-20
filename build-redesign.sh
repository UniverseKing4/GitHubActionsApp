#!/bin/bash

# GitCode Build Script
# Builds the redesigned Android IDE application

echo "🚀 Building GitCode..."
echo ""

cd "$(dirname "$0")"

# Check if gradlew exists
if [ ! -f "./gradlew" ]; then
    echo "❌ Error: gradlew not found"
    exit 1
fi

# Make gradlew executable
chmod +x ./gradlew

echo "📦 Cleaning previous builds..."
./gradlew clean

echo ""
echo "🔨 Building debug APK..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build successful!"
    echo ""
    echo "📱 APK location:"
    echo "   app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "🎨 UI/UX Redesign Features:"
    echo "   ✓ Material Design 3"
    echo "   ✓ Modern color system"
    echo "   ✓ Professional typography"
    echo "   ✓ Smooth animations"
    echo "   ✓ Card-based layouts"
    echo "   ✓ Enhanced code editor"
    echo "   ✓ Improved navigation"
    echo ""
else
    echo ""
    echo "❌ Build failed"
    exit 1
fi
