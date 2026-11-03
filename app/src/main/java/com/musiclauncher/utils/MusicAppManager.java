package com.musiclauncher.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Manages discovery and launching of music players on the device.
 * Intelligently targets the "Now Playing" playback screen.
 */
public class MusicAppManager {

    private static final String TAG = "MusicAppManager";

    // Known popular music players
    private static final Set<String> KNOWN_MUSIC_PACKAGES = new HashSet<>();
    static {
        KNOWN_MUSIC_PACKAGES.add("com.spotify.music");
        KNOWN_MUSIC_PACKAGES.add("com.google.android.apps.youtube.music");
        KNOWN_MUSIC_PACKAGES.add("com.apple.android.music");
        KNOWN_MUSIC_PACKAGES.add("maxmpz.audioplayer"); // Poweramp
        KNOWN_MUSIC_PACKAGES.add("code.name.monkey.retromusic"); // Retro Music
        KNOWN_MUSIC_PACKAGES.add("in.krosbits.musicolet"); // Musicolet
        KNOWN_MUSIC_PACKAGES.add("com.kodarkooperativet.blackplayerfree");
        KNOWN_MUSIC_PACKAGES.add("com.kodarkooperativet.blackplayerex");
        KNOWN_MUSIC_PACKAGES.add("com.deezer.android.app");
        KNOWN_MUSIC_PACKAGES.add("com.aspiro.tidal");
        KNOWN_MUSIC_PACKAGES.add("com.amazon.mp3");
        KNOWN_MUSIC_PACKAGES.add("com.soundcloud.android");
        KNOWN_MUSIC_PACKAGES.add("org.videolan.vlc");
        KNOWN_MUSIC_PACKAGES.add("com.sec.android.app.music"); // Samsung Music
        KNOWN_MUSIC_PACKAGES.add("com.google.android.music"); // Google Play Music (legacy)
        KNOWN_MUSIC_PACKAGES.add("com.kabouzeid.gramophone"); // Phonograph
        KNOWN_MUSIC_PACKAGES.add("com.rarlab.rar"); // occasional player
        KNOWN_MUSIC_PACKAGES.add("com.rhmsoft.pulsar"); // Pulsar
        KNOWN_MUSIC_PACKAGES.add("gonemad.gmmp"); // GoneMAD
        KNOWN_MUSIC_PACKAGES.add("com.aimp.player"); // AIMP
        KNOWN_MUSIC_PACKAGES.add("com.neutroncode.mp"); // Neutron
    }

    public static class MusicAppInfo implements Comparable<MusicAppInfo> {
        public final String packageName;
        public final String appName;
        public final Drawable icon;
        public final boolean isKnownMusicPlayer;

        public MusicAppInfo(String packageName, String appName, Drawable icon, boolean isKnownMusicPlayer) {
            this.packageName = packageName;
            this.appName = appName;
            this.icon = icon;
            this.isKnownMusicPlayer = isKnownMusicPlayer;
        }

        @Override
        public int compareTo(MusicAppInfo o) {
            if (this.isKnownMusicPlayer && !o.isKnownMusicPlayer) return -1;
            if (!this.isKnownMusicPlayer && o.isKnownMusicPlayer) return 1;
            return this.appName.compareToIgnoreCase(o.appName);
        }
    }

    /**
     * Discovers all installed music apps and audio players on the device.
     */
    public static List<MusicAppInfo> getInstalledMusicApps(Context context) {
        PackageManager pm = context.getPackageManager();
        List<MusicAppInfo> result = new ArrayList<>();
        Set<String> addedPackages = new HashSet<>();

        // 1. Check CATEGORY_APP_MUSIC
        Intent musicIntent = new Intent(Intent.ACTION_MAIN);
        musicIntent.addCategory(Intent.CATEGORY_APP_MUSIC);
        List<ResolveInfo> musicApps = pm.queryIntentActivities(musicIntent, 0);
        for (ResolveInfo ri : musicApps) {
            if (ri.activityInfo != null && !addedPackages.contains(ri.activityInfo.packageName)) {
                String pkg = ri.activityInfo.packageName;
                String label = ri.loadLabel(pm).toString();
                Drawable icon = ri.loadIcon(pm);
                result.add(new MusicAppInfo(pkg, label, icon, true));
                addedPackages.add(pkg);
            }
        }

        // 2. Check Audio View Intents
        Intent audioIntent = new Intent(Intent.ACTION_VIEW);
        audioIntent.setDataAndType(Uri.parse("file:///dummy.mp3"), "audio/*");
        List<ResolveInfo> audioApps = pm.queryIntentActivities(audioIntent, 0);
        for (ResolveInfo ri : audioApps) {
            if (ri.activityInfo != null && !addedPackages.contains(ri.activityInfo.packageName)) {
                String pkg = ri.activityInfo.packageName;
                String label = ri.loadLabel(pm).toString();
                Drawable icon = ri.loadIcon(pm);
                result.add(new MusicAppInfo(pkg, label, icon, true));
                addedPackages.add(pkg);
            }
        }

        // 3. Check Known Popular Music Players installed on the device
        for (String knownPkg : KNOWN_MUSIC_PACKAGES) {
            if (!addedPackages.contains(knownPkg)) {
                try {
                    ApplicationInfo appInfo = pm.getApplicationInfo(knownPkg, 0);
                    if (appInfo.enabled) {
                        String label = pm.getApplicationLabel(appInfo).toString();
                        Drawable icon = pm.getApplicationIcon(appInfo);
                        result.add(new MusicAppInfo(knownPkg, label, icon, true));
                        addedPackages.add(knownPkg);
                    }
                } catch (PackageManager.NameNotFoundException ignored) {
                    // Not installed
                }
            }
        }

        // 4. Also scan launcher activities to provide complete choice
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN, null);
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> allApps = pm.queryIntentActivities(launcherIntent, 0);
        for (ResolveInfo ri : allApps) {
            if (ri.activityInfo != null && !addedPackages.contains(ri.activityInfo.packageName)) {
                String pkg = ri.activityInfo.packageName;
                // Exclude this launcher itself
                if (Objects.equals(pkg, context.getPackageName())) {
                    continue;
                }
                String label = ri.loadLabel(pm).toString();
                Drawable icon = ri.loadIcon(pm);
                String lower = label.toLowerCase(Locale.ROOT);
                boolean looksLikeMusic = lower.contains("music")
                        || lower.contains("audio")
                        || lower.contains("player")
                        || lower.contains("podcast");
                result.add(new MusicAppInfo(pkg, label, icon, looksLikeMusic));
                addedPackages.add(pkg);
            }
        }

        Collections.sort(result);
        return result;
    }

    /**
     * Launches the user's selected music app.
     * Tries to target the active "Now Playing" playback screen where possible.
     */
    public static boolean launchMusicApp(Context context, String packageName, boolean targetNowPlaying) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return false;
        }

        PackageManager pm = context.getPackageManager();

        // Strategy 1: Check active MediaSessions on Android 5.0+ (API 21+)
        if (targetNowPlaying && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                MediaSessionManager msm = (MediaSessionManager) context.getSystemService(Context.MEDIA_SESSION_SERVICE);
                if (msm != null) {
                    List<MediaController> controllers;
                    try {
                        controllers = msm.getActiveSessions(null);
                    } catch (SecurityException se) {
                        // MediaSessionManager.getActiveSessions requires NotificationListenerService permission
                        Log.d(TAG, "No notification listener permission for active media sessions: " + se.getMessage());
                        controllers = null;
                    }

                    if (controllers != null) {
                        for (MediaController controller : controllers) {
                            if (Objects.equals(packageName, controller.getPackageName())) {
                                PendingIntent sessionActivity = controller.getSessionActivity();
                                if (sessionActivity != null) {
                                    try {
                                        Log.d(TAG, "Launching Now Playing via MediaSession PendingIntent: " + packageName);
                                        sessionActivity.send();
                                        return true;
                                    } catch (PendingIntent.CanceledException e) {
                                        Log.w(TAG, "Session PendingIntent canceled", e);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                Log.w(TAG, "Error checking media session", t);
            }
        }

        // Strategy 2: Try specific music player intent for this package
        if (targetNowPlaying) {
            try {
                Intent musicIntent = new Intent(Intent.ACTION_MAIN);
                musicIntent.addCategory(Intent.CATEGORY_APP_MUSIC);
                musicIntent.setPackage(packageName);
                musicIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                List<ResolveInfo> resolves = pm.queryIntentActivities(musicIntent, 0);
                if (!resolves.isEmpty()) {
                    context.startActivity(musicIntent);
                    return true;
                }
            } catch (Exception e) {
                Log.w(TAG, "Error launching CATEGORY_APP_MUSIC intent", e);
            }
        }

        // Strategy 3: Standard package launch intent with task restoration flags
        try {
            Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(launchIntent);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch package: " + packageName, e);
        }

        return false;
    }
}
