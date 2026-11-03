package com.musiclauncher.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.annotation.RequiresApi;

import com.musiclauncher.SettingsActivity;
import com.musiclauncher.utils.PreferenceManager;

/**
 * Quick Settings Tile service for Android 7.0+ (API 24+) to provide
 * direct 1-tap access to Launcher Settings from the notification shade.
 */
@RequiresApi(Build.VERSION_CODES.N)
public class MusicLauncherTileService extends TileService {

    @Override
    public void onStartListening() {
        super.onStartListening();
        Tile tile = getQsTile();
        if (tile != null) {
            PreferenceManager prefs = PreferenceManager.getInstance(this);
            String currentApp = prefs.getSelectedMusicAppName();
            if (currentApp != null && !currentApp.isEmpty()) {
                tile.setLabel(currentApp);
            }
            tile.setState(Tile.STATE_ACTIVE);
            tile.updateTile();
        }
    }

    @Override
    @android.annotation.SuppressLint("StartActivityAndCollapseDeprecated")
    public void onClick() {
        super.onClick();
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            startActivityAndCollapse(pendingIntent);
        } else {
            startActivityAndCollapse(intent);
        }
    }
}
