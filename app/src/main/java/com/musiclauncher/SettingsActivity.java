package com.musiclauncher;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.musiclauncher.adapter.MusicAppAdapter;
import com.musiclauncher.service.LauncherNotificationService;
import com.musiclauncher.utils.MusicAppManager;
import com.musiclauncher.utils.PreferenceManager;
import com.musiclauncher.utils.ThemeHelper;

import java.util.List;

/**
 * Launcher Settings Activity.
 * Runs in its own independent task affinity so it permanently appears in the
 * Android App Switcher (Recents), allowing users to access settings anytime.
 */
public class SettingsActivity extends AppCompatActivity {

    public static final String EXTRA_OPEN_PICKER = "extra_open_picker";
    public static final String EXTRA_SILENT_WARM = "extra_silent_warm";
    private static final int REQ_POST_NOTIFICATIONS = 2001;

    private ImageView mIvCurrentIcon;
    private TextView mTvCurrentName;
    private TextView mTvCurrentPkg;

    private TextView mTvSectionPlayer;
    private TextView mTvSectionTheme;
    private TextView mTvSectionBehavior;
    private TextView mTvSectionSystem;
    private TextView mTvCurrentThemeName;

    private Button mBtnChangePlayer;
    private Button mBtnSetDefaultHome;

    private SwitchCompat mSwitchAutoLaunch;
    private SwitchCompat mSwitchTargetNowPlaying;
    private SwitchCompat mSwitchPersistentNotif;
    private SwitchCompat mSwitchKeepInRecents;

    private TextView mTvVersionInfo;

    private PreferenceManager mPrefs;

    /**
     * Ensures this SettingsActivity task is registered and persistent in the Android Recent Apps list.
     */
    public static void ensureInRecents(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        intent.putExtra(EXTRA_SILENT_WARM, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        }
        try {
            // Only start if not already created
            context.startActivity(intent);
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);

        // If invoked purely to keep warm in recents, finish immediately so user stays where they were
        if (getIntent() != null && getIntent().getBooleanExtra(EXTRA_SILENT_WARM, false)) {
            // Android preserves the task entry in Recents even when finished
            // as documentLaunchMode="always" and autoRemoveFromRecents="false"
            moveTaskToBack(true);
        }

        setContentView(R.layout.activity_settings);
        mPrefs = PreferenceManager.getInstance(this);

        initViews();
        loadSettings();

        // Handle direct action from notification (e.g. "Switch Music")
        if (getIntent() != null && getIntent().getBooleanExtra(EXTRA_OPEN_PICKER, false)) {
            showMusicAppPickerDialog();
        }

        // On Android 13+ (API 33+), check for POST_NOTIFICATIONS runtime permission
        checkNotificationPermission();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent != null && intent.getBooleanExtra(EXTRA_OPEN_PICKER, false)) {
            showMusicAppPickerDialog();
        }
    }

    private void initViews() {
        mIvCurrentIcon = findViewById(R.id.iv_current_player_icon);
        mTvCurrentName = findViewById(R.id.tv_current_player_name);
        mTvCurrentPkg = findViewById(R.id.tv_current_player_pkg);
        mBtnChangePlayer = findViewById(R.id.btn_change_player);

        mTvSectionPlayer = findViewById(R.id.tv_section_player);
        mTvSectionTheme = findViewById(R.id.tv_section_theme);
        mTvSectionBehavior = findViewById(R.id.tv_section_behavior);
        mTvSectionSystem = findViewById(R.id.tv_section_system);
        mTvCurrentThemeName = findViewById(R.id.tv_current_theme_name);
        Button btnChangeTheme = findViewById(R.id.btn_change_theme);

        mSwitchAutoLaunch = findViewById(R.id.switch_auto_launch);
        mSwitchTargetNowPlaying = findViewById(R.id.switch_target_now_playing);
        mSwitchPersistentNotif = findViewById(R.id.switch_persistent_notif);
        mSwitchKeepInRecents = findViewById(R.id.switch_keep_in_recents);

        mBtnSetDefaultHome = findViewById(R.id.btn_set_default_home);
        Button btnOpenDrawer = findViewById(R.id.btn_settings_open_drawer);
        mTvVersionInfo = findViewById(R.id.tv_version_info);

        mBtnChangePlayer.setOnClickListener(v -> showMusicAppPickerDialog());
        btnChangeTheme.setOnClickListener(v -> showThemePickerDialog());
        mBtnSetDefaultHome.setOnClickListener(v -> openDefaultHomeSettings());
        btnOpenDrawer.setOnClickListener(v -> openAppDrawer());

        mSwitchAutoLaunch.setOnCheckedChangeListener((btn, isChecked) -> mPrefs.setAutoLaunchEnabled(isChecked));

        mSwitchTargetNowPlaying.setOnCheckedChangeListener((btn, isChecked) -> mPrefs.setTargetNowPlayingEnabled(isChecked));

        mSwitchPersistentNotif.setOnCheckedChangeListener((btn, isChecked) -> {
            mPrefs.setPersistentNotifEnabled(isChecked);
            if (isChecked) {
                LauncherNotificationService.startService(this);
            } else {
                LauncherNotificationService.stopService(this);
            }
        });

        mSwitchKeepInRecents.setOnCheckedChangeListener((btn, isChecked) -> mPrefs.setKeepInRecentsEnabled(isChecked));
    }

    private void loadSettings() {
        updateActivePlayerDisplay();
        applyCurrentThemeColors();

        mSwitchAutoLaunch.setChecked(mPrefs.isAutoLaunchEnabled());
        mSwitchTargetNowPlaying.setChecked(mPrefs.isTargetNowPlayingEnabled());
        mSwitchPersistentNotif.setChecked(mPrefs.isPersistentNotifEnabled());
        mSwitchKeepInRecents.setChecked(mPrefs.isKeepInRecentsEnabled());

        String versionInfo = getString(R.string.version_info,
                "1.0.0",
                Build.VERSION.RELEASE,
                Build.VERSION.SDK_INT);
        mTvVersionInfo.setText(versionInfo);
    }

    private void applyCurrentThemeColors() {
        int accentColor = ThemeHelper.getAccentColor(this);
        int cornerRadius = getResources().getDimensionPixelSize(R.dimen.button_corner_radius);

        ThemeHelper.ThemeItem currentItem = ThemeHelper.getThemeItem(mPrefs.getAppTheme());
        if (mTvCurrentThemeName != null) {
            mTvCurrentThemeName.setText(currentItem.nameRes);
        }

        if (mTvSectionPlayer != null) mTvSectionPlayer.setTextColor(accentColor);
        if (mTvSectionTheme != null) mTvSectionTheme.setTextColor(accentColor);
        if (mTvSectionBehavior != null) mTvSectionBehavior.setTextColor(accentColor);
        if (mTvSectionSystem != null) mTvSectionSystem.setTextColor(accentColor);

        if (mBtnChangePlayer != null) {
            ThemeHelper.applyAccentButton(mBtnChangePlayer, accentColor, cornerRadius);
        }
        if (mBtnSetDefaultHome != null) {
            mBtnSetDefaultHome.setTextColor(accentColor);
        }

        ThemeHelper.tintSwitch(mSwitchAutoLaunch, accentColor);
        ThemeHelper.tintSwitch(mSwitchTargetNowPlaying, accentColor);
        ThemeHelper.tintSwitch(mSwitchPersistentNotif, accentColor);
        ThemeHelper.tintSwitch(mSwitchKeepInRecents, accentColor);
    }

    private void showThemePickerDialog() {
        List<ThemeHelper.ThemeItem> themes = ThemeHelper.getAvailableThemes();
        String currentKey = mPrefs.getAppTheme();
        int checkedIndex = 0;
        CharSequence[] items = new CharSequence[themes.size()];
        for (int i = 0; i < themes.size(); i++) {
            ThemeHelper.ThemeItem item = themes.get(i);
            items[i] = getString(item.nameRes);
            if (item.key.equals(currentKey)) {
                checkedIndex = i;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.select_theme_dialog_title)
                .setSingleChoiceItems(items, checkedIndex, (dialog, which) -> {
                    ThemeHelper.ThemeItem selected = themes.get(which);
                    mPrefs.setAppTheme(selected.key);
                    applyCurrentThemeColors();
                    LauncherNotificationService.updateNotification(this);
                    Toast.makeText(this, getString(R.string.theme_applied, getString(selected.nameRes)), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void updateActivePlayerDisplay() {
        String selectedPkg = mPrefs.getSelectedMusicPackage();
        String selectedName = mPrefs.getSelectedMusicAppName();

        if (selectedPkg != null && !selectedPkg.isEmpty()) {
            mTvCurrentName.setText(selectedName != null ? selectedName : selectedPkg);
            mTvCurrentPkg.setText(selectedPkg);
            try {
                Drawable icon = getPackageManager().getApplicationIcon(selectedPkg);
                mIvCurrentIcon.setImageDrawable(icon);
            } catch (PackageManager.NameNotFoundException e) {
                mIvCurrentIcon.setImageResource(R.drawable.ic_music_note);
            }
        } else {
            mTvCurrentName.setText(R.string.no_app_selected);
            mTvCurrentPkg.setText(R.string.tap_to_select);
            mIvCurrentIcon.setImageResource(R.drawable.ic_music_note);
        }
    }

    private void showMusicAppPickerDialog() {
        List<MusicAppManager.MusicAppInfo> apps = MusicAppManager.getInstalledMusicApps(this);
        String currentPkg = mPrefs.getSelectedMusicPackage();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_music_picker, null);

        androidx.recyclerview.widget.RecyclerView rv = dialogView.findViewById(R.id.rv_music_picker);
        rv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

        AlertDialog dialog = builder.setTitle(R.string.select_music_app)
                .setView(dialogView)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        MusicAppAdapter adapter = new MusicAppAdapter(apps, currentPkg, appInfo -> {
            mPrefs.setSelectedMusicPackage(appInfo.packageName, appInfo.appName);
            updateActivePlayerDisplay();
            LauncherNotificationService.updateNotification(this);
            dialog.dismiss();
            Toast.makeText(this, "Default music player set to " + appInfo.appName, Toast.LENGTH_SHORT).show();
        });

        rv.setAdapter(adapter);
        dialog.show();
    }

    private void openDefaultHomeSettings() {
        // Universal Intent to choose default launcher across Android versions
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+
            intent = new Intent(Settings.ACTION_HOME_SETTINGS);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Android 7.0 - 9.0
            intent = new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
        } else {
            // Fallback for Android 4.4 KitKat to Android 6.0 Marshmallow
            intent = new Intent(Settings.ACTION_SETTINGS);
        }

        try {
            startActivity(intent);
        } catch (Exception e) {
            // Fallback to general settings
            try {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            } catch (Exception ex) {
                Toast.makeText(this, R.string.select_amp_launcher_toast, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void openAppDrawer() {
        Intent intent = new Intent(this, AppDrawerActivity.class);
        startActivity(intent);
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQ_POST_NOTIFICATIONS
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mPrefs.isPersistentNotifEnabled()) {
                    LauncherNotificationService.startService(this);
                }
            }
        }
    }
}
