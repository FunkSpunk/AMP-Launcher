package com.musiclauncher.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages configuration and persistent states for Music Launcher.
 */
public class PreferenceManager {

    private static final String PREF_NAME = "music_launcher_prefs";

    private static final String KEY_SELECTED_MUSIC_PKG = "key_selected_music_pkg";
    private static final String KEY_SELECTED_MUSIC_NAME = "key_selected_music_name";
    private static final String KEY_AUTO_LAUNCH = "key_auto_launch";
    private static final String KEY_TARGET_NOW_PLAYING = "key_target_now_playing";
    private static final String KEY_PERSISTENT_NOTIF = "key_persistent_notif";
    private static final String KEY_KEEP_IN_RECENTS = "key_keep_in_recents";
    private static final String KEY_FIRST_LAUNCH = "key_first_launch";
    private static final String KEY_THEME = "key_theme";

    public static final String THEME_EMERALD = "emerald";
    public static final String THEME_CYAN = "cyan";
    public static final String THEME_AMBER = "amber";
    public static final String THEME_PURPLE = "purple";
    public static final String THEME_CRIMSON = "crimson";
    public static final String THEME_OCEAN = "ocean";
    public static final String THEME_AMOLED = "amoled";

    private static volatile PreferenceManager sInstance;
    private final SharedPreferences mPrefs;

    private PreferenceManager(Context context) {
        mPrefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static PreferenceManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (PreferenceManager.class) {
                if (sInstance == null) {
                    sInstance = new PreferenceManager(context);
                }
            }
        }
        return sInstance;
    }

    public String getSelectedMusicPackage() {
        return mPrefs.getString(KEY_SELECTED_MUSIC_PKG, null);
    }

    public void setSelectedMusicPackage(String pkg, String appName) {
        mPrefs.edit()
                .putString(KEY_SELECTED_MUSIC_PKG, pkg)
                .putString(KEY_SELECTED_MUSIC_NAME, appName)
                .apply();
    }

    public String getSelectedMusicAppName() {
        return mPrefs.getString(KEY_SELECTED_MUSIC_NAME, null);
    }

    public boolean isAutoLaunchEnabled() {
        return mPrefs.getBoolean(KEY_AUTO_LAUNCH, true);
    }

    public void setAutoLaunchEnabled(boolean enabled) {
        mPrefs.edit().putBoolean(KEY_AUTO_LAUNCH, enabled).apply();
    }

    public boolean isTargetNowPlayingEnabled() {
        return mPrefs.getBoolean(KEY_TARGET_NOW_PLAYING, true);
    }

    public void setTargetNowPlayingEnabled(boolean enabled) {
        mPrefs.edit().putBoolean(KEY_TARGET_NOW_PLAYING, enabled).apply();
    }

    public boolean isPersistentNotifEnabled() {
        return mPrefs.getBoolean(KEY_PERSISTENT_NOTIF, true);
    }

    public void setPersistentNotifEnabled(boolean enabled) {
        mPrefs.edit().putBoolean(KEY_PERSISTENT_NOTIF, enabled).apply();
    }

    public boolean isKeepInRecentsEnabled() {
        return mPrefs.getBoolean(KEY_KEEP_IN_RECENTS, true);
    }

    public void setKeepInRecentsEnabled(boolean enabled) {
        mPrefs.edit().putBoolean(KEY_KEEP_IN_RECENTS, enabled).apply();
    }

    public boolean isFirstLaunch() {
        return mPrefs.getBoolean(KEY_FIRST_LAUNCH, true);
    }

    public void setFirstLaunchCompleted() {
        mPrefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply();
    }

    public String getAppTheme() {
        return mPrefs.getString(KEY_THEME, THEME_EMERALD);
    }

    public void setAppTheme(String theme) {
        mPrefs.edit().putString(KEY_THEME, theme).apply();
    }
}
