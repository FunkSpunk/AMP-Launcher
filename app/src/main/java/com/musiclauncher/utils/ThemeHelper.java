package com.musiclauncher.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.view.View;

import androidx.appcompat.widget.SwitchCompat;

import com.musiclauncher.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper to manage customizable app theme presets and dynamic accent coloring.
 * Full backward compatibility with Android 4.4 KitKat (API 19) through latest Android versions.
 */
public final class ThemeHelper {

    private ThemeHelper() {}

    public static class ThemeItem {
        public final String key;
        public final int nameRes;
        public final int accentColor;
        public final int darkColor;

        public ThemeItem(String key, int nameRes, int accentColor, int darkColor) {
            this.key = key;
            this.nameRes = nameRes;
            this.accentColor = accentColor;
            this.darkColor = darkColor;
        }
    }

    public static List<ThemeItem> getAvailableThemes() {
        List<ThemeItem> list = new ArrayList<>();
        list.add(new ThemeItem(PreferenceManager.THEME_EMERALD, R.string.theme_emerald, Color.parseColor("#1DB954"), Color.parseColor("#169C46")));
        list.add(new ThemeItem(PreferenceManager.THEME_CYAN, R.string.theme_cyan, Color.parseColor("#00E5FF"), Color.parseColor("#00B4D8")));
        list.add(new ThemeItem(PreferenceManager.THEME_AMBER, R.string.theme_amber, Color.parseColor("#FF9800"), Color.parseColor("#F57C00")));
        list.add(new ThemeItem(PreferenceManager.THEME_PURPLE, R.string.theme_purple, Color.parseColor("#BB86FC"), Color.parseColor("#9945FF")));
        list.add(new ThemeItem(PreferenceManager.THEME_CRIMSON, R.string.theme_crimson, Color.parseColor("#FF334B"), Color.parseColor("#D32F2F")));
        list.add(new ThemeItem(PreferenceManager.THEME_OCEAN, R.string.theme_ocean, Color.parseColor("#2979FF"), Color.parseColor("#1565C0")));
        list.add(new ThemeItem(PreferenceManager.THEME_AMOLED, R.string.theme_amoled, Color.parseColor("#FFFFFF"), Color.parseColor("#B3B3B3")));
        return list;
    }

    public static ThemeItem getThemeItem(String key) {
        for (ThemeItem item : getAvailableThemes()) {
            if (item.key.equals(key)) {
                return item;
            }
        }
        return getAvailableThemes().get(0);
    }

    public static int getAccentColor(Context context) {
        PreferenceManager prefs = PreferenceManager.getInstance(context);
        return getThemeItem(prefs.getAppTheme()).accentColor;
    }

    public static int getThemeResId(String key) {
        if (PreferenceManager.THEME_CYAN.equals(key)) {
            return R.style.Theme_AMPLauncher_Cyan;
        } else if (PreferenceManager.THEME_AMBER.equals(key)) {
            return R.style.Theme_AMPLauncher_Amber;
        } else if (PreferenceManager.THEME_PURPLE.equals(key)) {
            return R.style.Theme_AMPLauncher_Purple;
        } else if (PreferenceManager.THEME_CRIMSON.equals(key)) {
            return R.style.Theme_AMPLauncher_Crimson;
        } else if (PreferenceManager.THEME_OCEAN.equals(key)) {
            return R.style.Theme_AMPLauncher_Ocean;
        } else if (PreferenceManager.THEME_AMOLED.equals(key)) {
            return R.style.Theme_AMPLauncher_Amoled;
        } else {
            return R.style.Theme_AMPLauncher_Emerald;
        }
    }

    public static void applyTheme(Activity activity) {
        PreferenceManager prefs = PreferenceManager.getInstance(activity);
        activity.setTheme(getThemeResId(prefs.getAppTheme()));
    }

    /**
     * Dynamically sets background color and corner radius on a button,
     * maintaining KitKat 4.4 compatibility and ripple effects on API 21+.
     */
    public static void applyAccentButton(View button, int accentColor, int cornerRadiusPx) {
        if (button == null) return;
        GradientDrawable normalShape = new GradientDrawable();
        normalShape.setShape(GradientDrawable.RECTANGLE);
        normalShape.setColor(accentColor);
        normalShape.setCornerRadius(cornerRadiusPx);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ColorStateList rippleColor = ColorStateList.valueOf(Color.parseColor("#33FFFFFF"));
            RippleDrawable rippleDrawable = new RippleDrawable(rippleColor, normalShape, null);
            button.setBackground(rippleDrawable);
        } else {
            // KitKat (API 19) state list drawable
            GradientDrawable pressedShape = new GradientDrawable();
            pressedShape.setShape(GradientDrawable.RECTANGLE);
            pressedShape.setColor(Color.parseColor("#33FFFFFF"));
            pressedShape.setCornerRadius(cornerRadiusPx);

            StateListDrawable stateList = new StateListDrawable();
            stateList.addState(new int[]{android.R.attr.state_pressed}, pressedShape);
            stateList.addState(new int[]{}, normalShape);
            button.setBackground(stateList);
        }
    }

    /**
     * Styles a SwitchCompat with the selected theme accent color.
     */
    public static void tintSwitch(SwitchCompat switchCompat, int accentColor) {
        if (switchCompat == null) return;
        int offColor = Color.parseColor("#757575");
        int trackOffColor = Color.parseColor("#383838");
        int trackOnColor = Color.argb(128, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor));

        int[][] states = new int[][]{
                new int[]{-android.R.attr.state_checked},
                new int[]{android.R.attr.state_checked}
        };

        ColorStateList thumbColors = new ColorStateList(states, new int[]{offColor, accentColor});
        ColorStateList trackColors = new ColorStateList(states, new int[]{trackOffColor, trackOnColor});

        switchCompat.setThumbTintList(thumbColors);
        switchCompat.setTrackTintList(trackColors);
    }
}
