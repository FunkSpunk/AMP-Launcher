# AMP Launcher ⚡🎵

> A dedicated, high-performance Android system launcher that transforms your phone into an immersive, dedicated music player experience.  
> Fully supports **Android 4.4 KitKat (API 19)** through the latest **Android 17 (API 35+)**.
>
> <img width="1220" height="3392" alt="Screenshot_2026-11-03-21-22-20-784_com musiclauncher" src="https://github.com/user-attachments/assets/7a98e156-ae9e-44af-981c-89a7ca0e1731" />



---

## 📱 Key Features

1. **Music-First Home Screen Replacement**
   - Declares `android.intent.category.HOME` to act as your phone's primary system launcher.
   - Automatically directs to your chosen music player (Spotify, YouTube Music, Apple Music, Poweramp, Musicolet, Retro Music, etc.).

2. **Hardwired Home & Back Navigation**
   - **Home Button / Gesture:** Pressing Home from any screen returns straight to your selected default music player's **"Now Playing"** playback screen.
   - **Back Button / Gesture:** Pressing Back on the launcher or exiting out of apps intercepts the back action and brings the music player back to the front.
   - **Intelligent "Now Playing" Resolver:** Uses active `MediaSession` session activities to target the exact playback view, with fallback to task restoration and media intents.

3. **Customizable Theme Presets (Appearance & Setup Menu)**
   - **Custom Accent Themes:** Easily switch between customized theme presets in Settings:
     - 🟢 **Emerald Beat** (Spotify Green)
     - 🔵 **Cyber Cyan** (Electric Blue)
     - 🟠 **Sunset Gold** (Warm Amber)
     - 🟣 **Synthwave** (Neon Violet)
     - 🔴 **Crimson Rock** (Vivid Red)
     - 🌊 **Ocean Wave** (Cobalt Blue)
     - ⬛ **Midnight OLED** (Pure Black & White Minimalist)
   - Dynamic accent coloring automatically applies to buttons, switches, section headers, launcher status indicators, and notification drawer controls.
   - 100% backward-compatible with Android 4.4 KitKat (using programmatic `GradientDrawable`/`StateListDrawable` fallbacks) and modern Android `RippleDrawable`.

4. **App Switcher (Recents) Kept Intact**
   - The device's native App Switcher / Overview (swipe-up gesture or square button) remains 100% accessible to switch between any running apps.

5. **Persistent Launcher Settings Access**
   - **Always in Recent Apps:** Settings runs with an independent task affinity (`com.musiclauncher.settings`) and `documentLaunchMode="always"`. It permanently sits as a dedicated card in your Android App Switcher.
   - **Persistent System Notification:** An ongoing status bar notification provides 1-tap access to Settings, "Switch Music App", and "All Apps".
   - **Quick Settings Tile (Android 7.0+):** Add the "AMP Settings" tile to your quick settings panel.
   - **Safety Gesture:** Double-tapping or long-pressing anywhere on the launcher background opens Settings directly.

6. **Integrated App Drawer**
   - Searchable, clean app grid allows launching any installed app on your phone whenever needed.

---

## 🛠 Android Version Compatibility (KitKat 4.4 to Android 17)

| Android Version | API Level | Compatibility Implementation |
|---|---|---|
| **Android 4.4 KitKat** | API 19 | `SYSTEM_UI_FLAG_IMMERSIVE_STICKY`, `FLAG_TRANSLUCENT_STATUS`, MultiDex application support, programmatic theme tinting avoiding XML attribute inflation bugs |
| **Android 5.0 - 7.1** | API 21 - 25 | Material dark theme, `MediaSession` integration, document launch mode for Recents, `RippleDrawable` dynamic theming |
| **Android 7.0 - 7.1** | API 24 - 25 | Quick Settings Tile service (`MusicLauncherTileService`) |
| **Android 8.0 - 9.0** | API 26 - 28 | `NotificationChannel` (`IMPORTANCE_LOW`), adaptive launcher icons |
| **Android 10 - 11** | API 29 - 30 | Gesture navigation handling, `QUERY_ALL_PACKAGES` permission for music app discovery |
| **Android 12 - 13** | API 31 - 33 | `POST_NOTIFICATIONS` runtime permission, `PendingIntent.FLAG_IMMUTABLE`, `OnBackInvokedDispatcher` predictive back gestures |
| **Android 14 - 17** | API 34 - 36+ | Foreground service type `specialUse` with manifest property subtype, Edge-to-Edge window insets |

---

## 📁 Project Structure

```
music-launcher/
├── app/
│   ├── build.gradle                          # minSdk 19, targetSdk 35, AndroidX dependencies
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml               # HOME intent filter, separate taskAffinity for Settings
│       ├── java/com/musiclauncher/
│       │   ├── MusicLauncherApp.java         # MultiDex app, notification channel setup
│       │   ├── LauncherActivity.java         # Fullscreen Home launcher & Back/Home routing engine
│       │   ├── SettingsActivity.java         # Persistent Settings (in Recents & Notifications)
│       │   ├── AppDrawerActivity.java        # Searchable app drawer for all installed apps
│       │   ├── adapter/
│       │   │   ├── MusicAppAdapter.java      # Picker adapter for music players
│       │   │   └── AppAdapter.java           # App Drawer grid adapter
│       │   ├── receiver/
│       │   │   ├── BootReceiver.java         # Restores persistent notification on reboot
│       │   │   └── PackageChangeReceiver.java# Handles install/uninstall of music apps
│       │   ├── service/
│       │   │   ├── LauncherNotificationService.java # Ongoing notification controller
│       │   │   └── MusicLauncherTileService.java    # Android 7+ Quick Settings Tile
│       │   └── utils/
│       │       ├── ImmersiveModeHelper.java  # Fullscreen logic from KitKat to Android 17
│       │       ├── MusicAppManager.java      # Player discovery & Now Playing resolver
│       │       ├── PreferenceManager.java    # Persistent configuration manager
│       │       └── ThemeHelper.java          # Preset manager and dynamic theme applicator
│       └── res/                              # Layouts, vector drawables, adaptive icons, styles
├── build.gradle                              # Root Gradle script
├── settings.gradle
├── gradle.properties
└── gradlew.bat / gradlew                     # Gradle build wrappers
```

---

## 🚀 How to Build & Install

### Option A: Using Android Studio
1. Open **Android Studio**.
2. Select **Open an Existing Project** and navigate to:
   ```
   C:\Users\Funky\.gemini\antigravity\scratch\music-launcher
   ```
3. Allow Gradle to sync.
4. Connect an Android phone (or launch an emulator) running any version from Android 4.4 to Android 17.
5. Click **Run (Shift + F10)**.

### Option B: Using Command Line (Gradle)
```bash
# Navigate to project directory
cd music-launcher

# Build debug APK
./gradlew assembleDebug

# Output APK location:
# app/build/outputs/apk/debug/app-debug.apk
```

### Option C: Install via ADB
```bash
# Install to connected device
adb install -r app/build/outputs/apk/debug/app-debug.apk

# (Optional) Launch settings directly to set up
adb shell am start -n com.musiclauncher/.SettingsActivity
```

---

## ⚙️ Initial Setup on Device

1. Open **AMP Launcher**.
2. A dialog will prompt you to select your preferred default music app (e.g. Spotify, YouTube Music, Apple Music, Poweramp, etc.).
3. Press the device's **Home** button:
   - Android will display a prompt asking which app to use as Home.
   - Select **AMP Launcher** and tap **Always**.
4. Open **AMP Settings** to customize your theme accent (Emerald, Cyan, Amber, Purple, Crimson, Ocean, or Amoled).
5. On Android 13+, allow notification permissions so the persistent launcher settings notification stays pinned in your status bar.
6. You are done! Your Home and Back buttons are now permanently routed to your music player.
