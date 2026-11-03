package com.musiclauncher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.musiclauncher.service.LauncherNotificationService;
import com.musiclauncher.utils.PreferenceManager;

/**
 * Monitors package additions and removals.
 * If the user's currently selected music player was uninstalled, cleans up the preference
 * and updates the notification.
 */
public class PackageChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getData() == null) return;

        String action = intent.getAction();
        String packageName = intent.getData().getSchemeSpecificPart();

        if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
            PreferenceManager prefs = PreferenceManager.getInstance(context);
            if (java.util.Objects.equals(packageName, prefs.getSelectedMusicPackage())) {
                prefs.setSelectedMusicPackage(null, null);
                LauncherNotificationService.updateNotification(context);
            }
        }
    }
}
