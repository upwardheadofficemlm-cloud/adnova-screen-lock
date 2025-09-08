# Firebase Setup Guide for AdNova Screen Lock

This guide will help you configure your Firebase project for the AdNova Screen Lock application.

## ✅ **Setup Status - COMPLETED**

- ✅ **Firebase Project**: `adnova-screen-lock-90521` - Active
- ✅ **Authentication**: Admin user created (`admin@upwardmm.com`)
- ✅ **Firestore Database**: Security rules applied and active
- ✅ **Storage**: Security rules applied and active
- ✅ **Android App**: Configured with `google-services.json`
- ✅ **Dependencies**: Latest Firebase BoM 34.2.0 integrated

**Your Firebase project is fully configured and ready for the AdNova Screen Lock app!** 🎉

## 🔥 Firebase Project Configuration

Your Firebase project details:
- **Project ID**: `adnova-screen-lock-90521`
- **Project Number**: `914767381345`
- **Storage Bucket**: `gs://adnova-screen-lock-90521.firebasestorage.app`
- **API Key**: `AIzaSyAz8P_dX5uTI3zF0lW2CwPh4yqFvwfUn2o`
- **Mobile SDK App ID**: `1:914767381345:android:22ee0a507344a202977b20`

## 📱 Android App Configuration

### 1. Add Android App to Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: `adnova-screen-lock-90521`
3. Click "Add app" → Android
4. Enter package name: `com.adnova.screenlock`
5. Download the `google-services.json` file
6. Place it in the `app/` directory of your project

### 2. Enable Required Firebase Services

#### Authentication
1. Go to Authentication → Sign-in method
2. Enable Email/Password authentication
3. Create admin users for device management

#### Firestore Database
1. Go to Firestore Database
2. Create database in production mode
3. Set up security rules (see below)

#### Remote Config
1. Go to Remote Config
2. Add the following parameters:
   - `lock_configuration` (JSON string)
   - `max_unlock_attempts` (number)
   - `lock_timeout_minutes` (number)
   - `enable_analytics` (boolean)

#### Analytics
1. Go to Analytics
2. Enable Google Analytics
3. Link to your Firebase project

#### Cloud Messaging
1. Go to Cloud Messaging
2. Enable for your Android app
3. Configure server key for remote commands

#### Storage
1. Go to Storage
2. Create a new bucket or use the default bucket
3. Set up security rules (see below)
4. Configure storage rules for device logs and configurations

## 🔒 Firestore Security Rules

✅ **COMPLETED** - Firestore security rules have been successfully applied

**Current Firestore Rules:**
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Devices collection - only authenticated users can read/write
    match /devices/{deviceId} {
      allow read, write: if request.auth != null;
    }
    
    // Configurations collection - only authenticated users can read/write
    match /configurations/{configId} {
      allow read, write: if request.auth != null;
    }
    
    // Analytics collection - only authenticated users can write
    match /analytics/{analyticsId} {
      allow write: if request.auth != null;
      allow read: if request.auth != null;
    }
  }
}
```

**Status**: Rules are active and protecting your Firestore database

## 🗄️ Storage Security Rules

✅ **COMPLETED** - Storage security rules have been successfully applied

**Current Storage Rules:**
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    // Device logs - only authenticated users can read/write
    match /logs/{deviceId}_{timestamp}.txt {
      allow read, write: if request.auth != null;
    }
    
    // Device screenshots - only authenticated users can read/write
    match /screenshots/{deviceId}_{timestamp}.jpg {
      allow read, write: if request.auth != null;
    }
    
    // Device configurations - only authenticated users can read/write
    match /configurations/{deviceId}_config.json {
      allow read, write: if request.auth != null;
    }
    
    // Remote configurations - only authenticated users can read/write
    match /configurations/remote_config.json {
      allow read, write: if request.auth != null;
    }
  }
}
```

**Status**: Rules are active and protecting your Firebase Storage

## 👤 Authentication Setup

### Create Admin Users

✅ **COMPLETED** - Admin user has been created in Firebase Authentication

**Admin User Details:**
- **Email**: `admin@upwardmm.com`
- **Password**: `Upward103999@@`
- **Status**: Active and ready for device management

### Admin User Structure
```json
{
  "email": "admin@upwardmm.com",
  "password": "Upward103999@@",
  "role": "admin",
  "status": "active"
}
```

## 📊 Firestore Collections Structure

### Devices Collection
```json
{
  "deviceId": "unique_device_id",
  "configuration": {
    "lockType": "FULL_SCREEN",
    "floatingButtonEnabled": true,
    "doubleTapUnlock": true,
    "tapsToReveal": 3,
    "pinUnlockEnabled": false,
    "blockIconEnabled": true,
    "animationEnabled": true,
    "volumeButtonLock": false,
    "autoLockOnBoot": false,
    "kioskMode": false
  },
  "lastUpdated": 1640995200000,
  "appVersion": "1.0",
  "deviceInfo": {
    "model": "Android Device",
    "osVersion": "11",
    "screenSize": "1080x1920"
  }
}
```

### Analytics Collection
```json
{
  "deviceId": "unique_device_id",
  "eventType": "lock_started",
  "timestamp": 1640995200000,
  "data": {
    "lockType": "FULL_SCREEN",
    "duration": 3600000
  }
}
```

## 🔧 Remote Config Parameters

Set up these parameters in Firebase Remote Config:

### lock_configuration
- **Key**: `lock_configuration`
- **Type**: JSON
- **Default Value**:
```json
{
  "lockType": "FULL_SCREEN",
  "floatingButtonEnabled": true,
  "doubleTapUnlock": true,
  "tapsToReveal": 3,
  "pinUnlockEnabled": false,
  "blockIconEnabled": true,
  "animationEnabled": true,
  "volumeButtonLock": false,
  "autoLockOnBoot": false,
  "kioskMode": false
}
```

### max_unlock_attempts
- **Key**: `max_unlock_attempts`
- **Type**: Number
- **Default Value**: `5`

### lock_timeout_minutes
- **Key**: `lock_timeout_minutes`
- **Type**: Number
- **Default Value**: `0` (no timeout)

### enable_analytics
- **Key**: `enable_analytics`
- **Type**: Boolean
- **Default Value**: `true`

## 📱 Cloud Messaging Setup

### Server Key
1. Go to Project Settings → Cloud Messaging
2. Copy the Server Key
3. Use this for sending remote commands

### Message Types
The app handles these message types:
- `lock_config` - Update device configuration
- `lock_command` - Start/stop lock commands
- `remote_config` - Remote configuration updates

### Example Message
```json
{
  "to": "device_token",
  "data": {
    "type": "lock_command",
    "command": "start_lock"
  }
}
```

## 🚀 Testing the Setup

### 1. Build and Install
```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 2. Test Firebase Connection
1. Open the app
2. Check if Firebase services are working
3. Verify authentication
4. Test remote config fetch

### 3. Test Remote Management
1. Login as admin
2. Send configuration updates
3. Monitor analytics
4. Test remote commands

## 🔍 Troubleshooting

### Common Issues

1. **Authentication Failed**
   - Check if Email/Password is enabled
   - Verify admin user credentials
   - Check Firestore security rules

2. **Remote Config Not Working**
   - Verify parameters are set in Firebase Console
   - Check if app has internet connection
   - Verify API key is correct

3. **Analytics Not Tracking**
   - Check if Analytics is enabled
   - Verify measurement ID
   - Check if events are being sent

4. **Cloud Messaging Not Working**
   - Verify server key
   - Check device token registration
   - Verify message format

### Debug Steps

1. Check Firebase Console for errors
2. Monitor app logs for Firebase issues
3. Verify network connectivity
4. Test with Firebase emulator

## 📞 Support

If you encounter issues:
1. Check Firebase Console for service status
2. Review app logs for error messages
3. Verify all services are properly enabled
4. Test with a simple Firebase app first

---

Your Firebase project is now configured for the AdNova Screen Lock application! 🎉
