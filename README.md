# AdNova Screen Lock

A comprehensive Android application for digital signage screen locking with advanced features and Firebase backend integration.

## 📱 Overview

AdNova Screen Lock is designed for digital signage devices to lock touch input on full or partial screen areas, providing secure and flexible screen protection for kiosk and display applications.

## ✨ Features

### Core Locking Features
- **Full Screen Lock**: Complete screen touch blocking
- **Edge Lock**: Lock left, right, or both screen edges
- **Custom Area Lock**: User-defined rectangular lock areas
- **Floating Unlock Button**: Double-tap to unlock with customizable tap count
- **PIN Protection**: Optional 4-6 digit PIN for additional security

### Advanced Features
- **Auto-lock on Boot**: Automatically start locking when device boots
- **Kiosk Mode**: Block system buttons and prevent app switching
- **Volume Button Lock**: Disable volume buttons during lock
- **Touch Animation**: Visual feedback on locked area touches
- **Block Icon Display**: Show blocked touch indicators
- **Whitelist Apps**: Allow specific apps while screen is locked

### Firebase Integration
- **Authentication**: Admin login system
- **Firestore**: Store device configurations
- **Remote Config**: Push configuration updates remotely
- **Analytics**: Track lock/unlock events and errors
- **Cloud Messaging**: Remote commands and notifications

### Security Features
- **Encrypted Storage**: Secure PIN and configuration storage
- **Device Admin**: Enhanced kiosk mode capabilities
- **Overlay Permissions**: Proper system permission handling
- **Lockout Protection**: PIN attempt limiting with timeouts

## 🛠 Technical Specifications

- **Platform**: Android 10+ (API 29+)
- **Language**: Kotlin
- **Architecture**: MVVM with Coroutines
- **Backend**: Firebase (Auth, Firestore, Remote Config, Analytics)
- **Storage**: Encrypted SharedPreferences
- **UI**: Material Design 3

## 📋 Requirements

### Hardware
- **RAM**: 2GB minimum
- **Storage**: 32GB minimum
- **Display**: Touch screen support

### Permissions
- `SYSTEM_ALERT_WINDOW` - Overlay permission for screen locking
- `RECEIVE_BOOT_COMPLETED` - Auto-lock on boot
- `ACCESS_NETWORK_STATE` - Firebase connectivity
- `WAKE_LOCK` - Keep screen on in kiosk mode
- `FOREGROUND_SERVICE` - Persistent lock service
- `VIBRATE` - Haptic feedback
- `DISABLE_KEYGUARD` - Kiosk mode functionality

## 🚀 Installation

### Prerequisites
Before building the project, you need to set up your development environment. Follow the detailed guide in `DEVELOPMENT_SETUP.md`.

**Quick Setup:**
1. Install Java 17+ (using Homebrew: `brew install openjdk@17`)
2. Install Android Studio (optional but recommended)
3. Set up Android SDK and environment variables

### Building the Project

1. **Setup Firebase**
   - Your Firebase project is already configured: `adnova-screen-lock-90521`
   - The `google-services.json` file is included with your project configuration
   - Follow the detailed setup guide in `FIREBASE_SETUP.md`
   - Enable Authentication, Firestore, Remote Config, and Analytics in Firebase Console

2. **Build the project**
   ```bash
   ./gradlew assembleDebug
   ```

3. **Install on device**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### Development Setup
For detailed development environment setup, see `DEVELOPMENT_SETUP.md`.

## ⚙️ Configuration

### Initial Setup
1. Grant overlay permission when prompted
2. Configure lock type and settings
3. Set PIN if required
4. Enable auto-lock if needed

### Firebase Setup
1. Create admin user in Firebase Authentication
2. Configure Firestore security rules
3. Set up Remote Config parameters
4. Configure Cloud Messaging for remote commands

### Kiosk Mode Setup
1. Enable device admin permission
2. Configure lock task packages
3. Set up accessibility service (optional)

## 📖 Usage

### Basic Locking
1. Open the app
2. Select lock type (Full Screen, Edge, Custom Area)
3. Configure unlock settings
4. Tap "Start Lock" to begin

### PIN Protection
1. Enable PIN unlock in settings
2. Set a 4-6 digit PIN
3. PIN will be required to unlock screen

### Kiosk Mode
1. Enable kiosk mode in settings
2. Grant device admin permission
3. App will prevent system button access

### Remote Management
1. Admin login via Firebase
2. Push configurations remotely
3. Monitor device status and analytics

## 🔧 Development

### Project Structure
```
app/src/main/java/com/adnova/screenlock/
├── data/           # Data models and preferences
├── firebase/       # Firebase integration
├── receiver/       # Broadcast receivers
├── service/        # Background services
├── ui/            # Activities and UI components
└── utils/         # Utility classes
```

### Key Components
- **MainActivity**: Main control interface
- **SettingsActivity**: Configuration settings
- **LockOverlayService**: Core locking service
- **FirebaseManager**: Backend integration
- **PinManager**: PIN security handling
- **KioskModeManager**: Kiosk functionality

### Building
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew test
```

## 🔒 Security Considerations

- PINs are hashed with salt before storage
- Configuration data is encrypted
- Device admin permissions are properly managed
- Overlay permissions are validated
- Firebase security rules should be configured

## 🐛 Troubleshooting

### Common Issues
1. **Overlay permission denied**: Grant permission in device settings
2. **Lock not working**: Check overlay permission and service status
3. **PIN not working**: Verify PIN is set correctly
4. **Kiosk mode issues**: Ensure device admin permission is granted

### Debug Mode
Enable debug logging by setting `BuildConfig.DEBUG` to true in development builds.

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📞 Support

For support and questions:
- Create an issue in the repository
- Contact the development team
- Check the documentation

## 🔄 Version History

- **v1.0.0** - Initial release with core locking features
- **v1.1.0** - Added Firebase integration
- **v1.2.0** - Enhanced kiosk mode and security features

---

**AdNova Screen Lock** - Secure digital signage screen protection solution.
