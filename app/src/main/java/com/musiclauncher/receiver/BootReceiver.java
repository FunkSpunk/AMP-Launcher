package com.musiclauncher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.musiclauncher.SettingsActivity;
import com.musiclauncher.service.LauncherNotificationService;
import com.musiclauncher.utils.PreferenceManager;

/**
 * Ensures the persistent notification and launcher settings task are restored
 * upon device boot or app update.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)
                || "android.intent.action.QUICKBOOT_POWERON".equals(action)) {

            PreferenceManager prefs = PreferenceManager.getInstance(context);

            // Restore persistent notification if enabled
            if (prefs.isPersistentNotifEnabled()) {
                LauncherNotificationService.startService(context);
            }

            // Ensure settings card is populated in Recent Apps if enabled
            if (prefs.isKeepInRecentsEnabled()) {
                SettingsActivity.ensureInRecents(context);
            }
        }
    }
}
