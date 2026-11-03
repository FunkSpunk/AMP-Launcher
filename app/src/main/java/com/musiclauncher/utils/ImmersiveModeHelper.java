package com.musiclauncher.utils;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * Universal System Window & Edge-to-Edge Helper.
 * Preserves the user's default system navigation mode (fullscreen gesture navigation
 * or 3-button navigation) while providing an edge-to-edge layout with transparent
 * system bars across Android 4.4 KitKat (API 19) through Android 17 (API 35+).
 */
public final class ImmersiveModeHelper {

    private ImmersiveModeHelper() {}

    /**
     * Applies edge-to-edge styling while keeping the user's default system navigation active.
     */
    @SuppressWarnings("deprecation")
    public static void applyImmersiveMode(Activity activity) {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        Window window = activity.getWindow();
        if (window == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30 to Android 17):
            // Render edge-to-edge under transparent system bars.
            // Do NOT hide navigation bars so Android's native gesture navigation
            // or user-selected 3-button navigation remains fully preserved.
            WindowCompat.setDecorFitsSystemWindows(window, false);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);

            WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
            if (controller != null) {
                // Ensure navigation bars are always visible according to user system settings
                controller.show(WindowInsetsCompat.Type.navigationBars());
                // Dark background => light icons on status and navigation bars
                controller.setAppearanceLightStatusBars(false);
                controller.setAppearanceLightNavigationBars(false);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Android 5.0 Lollipop (API 21) through Android 10 (API 29):
            // LAYOUT flags allow content to flow behind transparent system bars,
            // while leaving the navigation bar visible and interactive.
            WindowCompat.setDecorFitsSystemWindows(window, false);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);

            View decorView = window.getDecorView();
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            decorView.setSystemUiVisibility(flags);
        } else {
            // Android 4.4 KitKat (API 19): Translucent system bars
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }
}
