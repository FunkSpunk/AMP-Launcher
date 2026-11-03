package com.musiclauncher;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDexApplication;

import com.musiclauncher.service.LauncherNotificationService;
import com.musiclauncher.utils.PreferenceManager;

/**
 * Main Application class for Music Launcher.
 * Extends MultiDexApplication to support Android 4.4 KitKat (API 19) through latest Android 17.
 */
public class MusicLauncherApp extends MultiDexApplication {

    public static final String NOTIFICATION_CHANNEL_ID = "music_launcher_channel";

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable vector drawables support for pre-Lollipop devices (API 19 KitKat)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        // Initialize Notification Channel on Android 8.0+ (Oreo, API 26+)
        createNotificationChannel();

        // Start persistent notification service if enabled in preferences
        PreferenceManager prefs = PreferenceManager.getInstance(this);
        if (prefs.isPersistentNotifEnabled()) {
            LauncherNotificationService.startService(this);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.notif_channel_name);
            String description = getString(R.string.notif_channel_desc);
            // IMPORTANCE_LOW ensures ongoing notification stays in the shade without audible buzz
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setShowBadge(false);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
