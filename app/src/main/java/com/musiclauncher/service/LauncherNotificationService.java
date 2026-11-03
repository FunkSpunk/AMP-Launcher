package com.musiclauncher.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.musiclauncher.AppDrawerActivity;
import com.musiclauncher.MusicLauncherApp;
import com.musiclauncher.R;
import com.musiclauncher.SettingsActivity;
import com.musiclauncher.utils.PreferenceManager;
import com.musiclauncher.utils.ThemeHelper;

/**
 * Persistent Foreground Service ensuring the Launcher Settings and quick controls
 * are always available in the Android system notification drawer.
 */
public class LauncherNotificationService extends Service {

    public static final int NOTIFICATION_ID = 1001;
    public static final String ACTION_UPDATE_NOTIFICATION = "com.musiclauncher.ACTION_UPDATE_NOTIFICATION";
    public static final String ACTION_STOP_SERVICE = "com.musiclauncher.ACTION_STOP_SERVICE";

    public static void startService(Context context) {
        Intent intent = new Intent(context, LauncherNotificationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, intent);
        } else {
            context.startService(intent);
        }
    }

    public static void updateNotification(Context context) {
        Intent intent = new Intent(context, LauncherNotificationService.class);
        intent.setAction(ACTION_UPDATE_NOTIFICATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stopService(Context context) {
        Intent intent = new Intent(context, LauncherNotificationService.class);
        intent.setAction(ACTION_STOP_SERVICE);
        context.startService(intent);
    }

    @Override
    @SuppressWarnings("deprecation")
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP_SERVICE.equals(intent.getAction())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE);
            } else {
                stopForeground(true);
            }
            stopSelf();
            return START_NOT_STICKY;
        }

        startForegroundWithNotification();
        return START_STICKY;
    }

    private void startForegroundWithNotification() {
        Notification notification = buildNotification();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ foreground service types
            int type = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                    ? ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    : 0;
            startForeground(NOTIFICATION_ID, notification, type);
        } else {
            // Android 4.4 KitKat to Android 9 Pie
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    private Notification buildNotification() {
        PreferenceManager prefs = PreferenceManager.getInstance(this);
        String currentApp = prefs.getSelectedMusicAppName();
        if (currentApp == null || currentApp.trim().isEmpty()) {
            currentApp = getString(R.string.no_app_selected);
        }

        int pendingFlags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;

        // Intent to open Settings Activity
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pSettings = PendingIntent.getActivity(this, 101, settingsIntent, pendingFlags);

        // Intent to open Settings directly to the Music App picker
        Intent switchMusicIntent = new Intent(this, SettingsActivity.class);
        switchMusicIntent.putExtra(SettingsActivity.EXTRA_OPEN_PICKER, true);
        switchMusicIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pSwitchMusic = PendingIntent.getActivity(this, 102, switchMusicIntent, pendingFlags);

        // Intent to open App Drawer
        Intent appsIntent = new Intent(this, AppDrawerActivity.class);
        appsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pApps = PendingIntent.getActivity(this, 103, appsIntent, pendingFlags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MusicLauncherApp.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music_note)
                .setColor(ThemeHelper.getAccentColor(this))
                .setContentTitle(getString(R.string.notif_title))
                .setContentText(getString(R.string.notif_text, currentApp))
                .setContentIntent(pSettings)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(R.drawable.ic_settings, getString(R.string.notif_action_settings), pSettings)
                .addAction(R.drawable.ic_refresh, getString(R.string.notif_action_switch), pSwitchMusic)
                .addAction(R.drawable.ic_apps, getString(R.string.notif_action_apps), pApps);

        return builder.build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
