# Development Setup Guide for AdNova Screen Lock

This guide will help you set up your development environment to build and test the AdNova Screen Lock Android application.

## 🛠 Prerequisites

### 1. Install Java Development Kit (JDK)

#### Option A: Install using Homebrew (Recommended)
```bash
# Install Homebrew if not already installed
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install OpenJDK 17 (recommended for Android development)
brew install openjdk@17

# Add to your shell profile (~/.zshrc or ~/.bash_profile)
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
echo 'export JAVA_HOME="/opt/homebrew/opt/openjdk@17"' >> ~/.zshrc
source ~/.zshrc
```

#### Option B: Download from Oracle
1. Go to [Oracle JDK Downloads](https://www.oracle.com/java/technologies/downloads/)
2. Download JDK 17 for macOS
3. Install the package
4. Set JAVA_HOME environment variable

#### Option C: Install using SDKMAN
```bash
# Install SDKMAN
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install Java 17
sdk install java 17.0.2-open
sdk use java 17.0.2-open
```

### 2. Install Android Studio (Recommended)

1. Download [Android Studio](https://developer.android.com/studio)
2. Install Android Studio
3. Open Android Studio and install:
   - Android SDK
   - Android SDK Platform-Tools
   - Android SDK Build-Tools
   - Android Emulator (optional)

### 3. Set up Android SDK Environment Variables

Add these to your shell profile (~/.zshrc or ~/.bash_profile):

```bash
# Android SDK
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/emulator
export PATH=$PATH:$ANDROID_HOME/platform-tools
export PATH=$PATH:$ANDROID_HOME/tools
export PATH=$PATH:$ANDROID_HOME/tools/bin
```

## 🚀 Building the Project

### Method 1: Using Android Studio (Recommended)

1. Open Android Studio
2. Select "Open an existing Android Studio project"
3. Navigate to the AdNovaScreenLock folder
4. Wait for Gradle sync to complete
5. Click "Build" → "Build Bundle(s) / APK(s)" → "Build APK(s)"

### Method 2: Using Command Line

After installing Java and setting up environment variables:

```bash
# Navigate to project directory
cd AdNovaScreenLock

# Make gradlew executable (if not already)
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build
./gradlew clean

# Run tests
./gradlew test
```

## 📱 Testing the App

### Option 1: Physical Device

1. Enable Developer Options on your Android device:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
   - Go back to Settings → Developer Options
   - Enable "USB Debugging"

2. Connect device via USB
3. Run the app:
   ```bash
   ./gradlew installDebug
   ```

### Option 2: Android Emulator

1. Open Android Studio
2. Go to Tools → AVD Manager
3. Create a new Virtual Device
4. Select a device definition (e.g., Pixel 6)
5. Download and select a system image (API 29+)
6. Start the emulator
7. Run the app from Android Studio

## 🔧 Troubleshooting

### Java Issues

**Problem**: "Unable to locate a Java Runtime"
**Solution**: 
```bash
# Check if Java is installed
java -version

# If not installed, install using Homebrew
brew install openjdk@17

# Set JAVA_HOME
export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
export PATH="$JAVA_HOME/bin:$PATH"
```

### Gradle Issues

**Problem**: Gradle build fails
**Solution**:
```bash
# Clean and rebuild
./gradlew clean
./gradlew assembleDebug

# Check Gradle version
./gradlew --version
```

### Android SDK Issues

**Problem**: Android SDK not found
**Solution**:
```bash
# Set ANDROID_HOME
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools

# Verify SDK installation
ls $ANDROID_HOME/platforms
```

### Firebase Issues

**Problem**: Firebase services not working
**Solution**:
1. Verify `google-services.json` is in the `app/` directory
2. Check Firebase project configuration
3. Ensure all Firebase services are enabled in Firebase Console
4. Verify package name matches Firebase project

## 📋 Development Checklist

- [ ] Java 17+ installed and configured
- [ ] Android Studio installed (optional but recommended)
- [ ] Android SDK installed
- [ ] Environment variables set (JAVA_HOME, ANDROID_HOME)
- [ ] Firebase project configured
- [ ] `google-services.json` file in place
- [ ] Project builds successfully
- [ ] App runs on device/emulator

## 🎯 Next Steps

1. **Set up Firebase services** in Firebase Console
2. **Configure authentication** for admin users
3. **Set up Firestore** database and security rules
4. **Configure Remote Config** parameters
5. **Test all features** on a physical device
6. **Deploy to production** when ready

## 📞 Getting Help

If you encounter issues:

1. Check the troubleshooting section above
2. Review Firebase setup guide (`FIREBASE_SETUP.md`)
3. Check Android Studio logs for detailed error messages
4. Verify all prerequisites are installed correctly

---

Your development environment is now ready for AdNova Screen Lock development! 🎉
