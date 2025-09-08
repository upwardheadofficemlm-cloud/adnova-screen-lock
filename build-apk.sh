#!/bin/bash

# AdNova Screen Lock - APK Build Script
# This script builds the Android APK and prepares it for distribution

echo "🚀 Building AdNova Screen Lock APK..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Check if Java is installed
if ! command -v java &> /dev/null; then
    print_error "Java is not installed. Please install Java 17 or higher."
    print_info "Install Java using: brew install openjdk@17"
    exit 1
fi

# Check if Android SDK is available
if [ ! -d "$ANDROID_HOME" ]; then
    print_warning "ANDROID_HOME not set. Using default Android SDK path."
    export ANDROID_HOME="$HOME/Library/Android/sdk"
fi

# Check if gradlew exists
if [ ! -f "./gradlew" ]; then
    print_error "gradlew not found. Make sure you're in the project root directory."
    exit 1
fi

# Make gradlew executable
chmod +x ./gradlew

print_info "Starting APK build process..."

# Clean previous builds
print_info "Cleaning previous builds..."
./gradlew clean

# Build debug APK
print_info "Building debug APK..."
./gradlew assembleDebug

# Check if build was successful
if [ $? -eq 0 ]; then
    print_status "APK build completed successfully!"
    
    # Find the generated APK
    APK_PATH=$(find app/build/outputs/apk/debug -name "*.apk" | head -1)
    
    if [ -f "$APK_PATH" ]; then
        print_status "APK found: $APK_PATH"
        
        # Create downloads directory
        mkdir -p website/downloads
        
        # Copy APK to downloads directory with proper name
        APK_NAME="AdNovaScreenLock-v1.0.0.apk"
        cp "$APK_PATH" "website/downloads/$APK_NAME"
        
        print_status "APK copied to website/downloads/$APK_NAME"
        
        # Get file size
        FILE_SIZE=$(ls -lh "website/downloads/$APK_NAME" | awk '{print $5}')
        print_info "APK size: $FILE_SIZE"
        
        # Create beta version (same file for now)
        cp "website/downloads/$APK_NAME" "website/downloads/AdNovaScreenLock-beta.apk"
        print_status "Beta version created"
        
        # Create release notes
        cat > website/downloads/RELEASE_NOTES.md << EOF
# AdNova Screen Lock v1.0.0

## Release Date
$(date '+%B %d, %Y')

## What's New
- Initial release of AdNova Screen Lock
- Full screen, edge, and custom area locking
- PIN protection and kiosk mode
- Firebase backend integration
- Real-time device management
- Admin dashboard for remote control

## System Requirements
- Android 10.0 (API level 29) or higher
- 2GB RAM minimum
- 32GB storage minimum
- Internet connection for Firebase features

## Installation Instructions
1. Download the APK file
2. Enable "Install from Unknown Sources" in Android Settings
3. Open the downloaded APK file
4. Follow the installation prompts
5. Grant overlay permission when prompted

## Features
- Multiple lock types (full, edge, custom)
- Floating unlock button
- PIN protection
- Kiosk mode
- Firebase integration
- Remote device management
- Real-time monitoring
- Admin dashboard

## Support
For support and questions, visit our website or contact us at support@adnova.com
EOF
        
        print_status "Release notes created"
        
        # Create download info JSON
        cat > website/downloads/download-info.json << EOF
{
  "version": "1.0.0",
  "buildDate": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
  "fileSize": "$FILE_SIZE",
  "downloads": {
    "latest": {
      "url": "AdNovaScreenLock-v1.0.0.apk",
      "version": "1.0.0",
      "type": "stable"
    },
    "beta": {
      "url": "AdNovaScreenLock-beta.apk",
      "version": "1.0.0-beta",
      "type": "beta"
    }
  },
  "requirements": {
    "minAndroid": "10.0",
    "minRam": "2GB",
    "minStorage": "32GB"
  }
}
EOF
        
        print_status "Download info JSON created"
        
        print_status "Build process completed successfully!"
        print_info "APK files are ready in website/downloads/"
        print_info "You can now deploy the website with the APK files"
        
    else
        print_error "APK file not found after build"
        exit 1
    fi
    
else
    print_error "APK build failed"
    exit 1
fi

print_info "Build script completed!"
