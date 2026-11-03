package com.musiclauncher;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.musiclauncher.adapter.MusicAppAdapter;
import com.musiclauncher.service.LauncherNotificationService;
import com.musiclauncher.utils.ImmersiveModeHelper;
import com.musiclauncher.utils.MusicAppManager;
import com.musiclauncher.utils.PreferenceManager;
import com.musiclauncher.utils.ThemeHelper;

import java.util.List;

/**
 * Main System Launcher Activity.
 * Replaces the default Android home screen with the user's selected music player UI.
 * Ensures Home button, Home gesture, and Back button take the user back
 * to the selected music player's "Now Playing" playback screen.
 */
public class LauncherActivity extends AppCompatActivity {

    private ImageView mIvMusicIcon;
    private TextView mTvMusicName;
    private TextView mTvMusicSubtitle;
    private Button mBtnLaunchMusic;
    private Button mBtnSelectMusicApp;
    private View mRootView;

    private PreferenceManager mPrefs;
    private Handler mHandler;
    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        mPrefs = PreferenceManager.getInstance(this);
        mHandler = new Handler(Looper.getMainLooper());

        initViews();
        setupGestures();
        setupBackHandling();

        // Ensure launcher settings card exists in the Recent Apps list
        if (mPrefs.isKeepInRecentsEnabled()) {
            SettingsActivity.ensureInRecents(this);
        }

        // Ensure persistent notification is active
        if (mPrefs.isPersistentNotifEnabled()) {
            LauncherNotificationService.startService(this);
        }
    }

    private void initViews() {
        mRootView = findViewById(R.id.launcher_root);
        mIvMusicIcon = findViewById(R.id.iv_music_app_icon);
        mTvMusicName = findViewById(R.id.tv_music_app_name);
        mTvMusicSubtitle = findViewById(R.id.tv_music_subtitle);
        mBtnLaunchMusic = findViewById(R.id.btn_launch_music);
        mBtnSelectMusicApp = findViewById(R.id.btn_select_music_app);
        Button btnOpenSettings = findViewById(R.id.btn_open_settings);
        Button btnOpenDrawer = findViewById(R.id.btn_open_drawer);

        mBtnLaunchMusic.setOnClickListener(v -> triggerMusicAppLaunch());
        mBtnSelectMusicApp.setOnClickListener(v -> showMusicAppPickerDialog());
        btnOpenSettings.setOnClickListener(v -> openSettings());
        btnOpenDrawer.setOnClickListener(v -> openAppDrawer());

        // Tapping background also brings up music player
        mRootView.setOnClickListener(v -> triggerMusicAppLaunch());
    }

    @android.annotation.SuppressLint("ClickableViewAccessibility")
    private void setupGestures() {
        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(@androidx.annotation.NonNull MotionEvent e) {
                // Long press anywhere on the launcher screen opens Launcher Settings
                openSettings();
            }

            @Override
            public boolean onDoubleTap(@androidx.annotation.NonNull MotionEvent e) {
                // Double tap anywhere on launcher opens Launcher Settings
                openSettings();
                return true;
            }
        });

        mRootView.setOnTouchListener((v, event) -> {
            mGestureDetector.onTouchEvent(event);
            return false;
        });
    }

    private void setupBackHandling() {
        // Universal Back gesture / key handler:
        // AndroidX OnBackPressedCallback seamlessly intercepts back actions on Android 4.4 KitKat (API 19)
        // all the way to Android 14+ predictive back gestures without risking Dalvik classload issues.
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackAction();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 1. Re-apply full screen immersive mode across all Android versions
        ImmersiveModeHelper.applyImmersiveMode(this);

        // 2. Refresh UI with currently selected music app info
        updateLauncherUi();

        // 3. Routing Engine:
        // When user taps Home or exits music app via Back, Android activates this launcher.
        // If auto-launch is enabled and a music app is chosen, immediately launch it!
        String selectedPkg = mPrefs.getSelectedMusicPackage();
        if (selectedPkg != null && !selectedPkg.isEmpty() && mPrefs.isAutoLaunchEnabled()) {
            // Tiny delay ensures window transitions settle smoothly on modern gesture navigation
            mHandler.postDelayed(this::triggerMusicAppLaunch, 150);
        } else if (selectedPkg == null && mPrefs.isFirstLaunch()) {
            mPrefs.setFirstLaunchCompleted();
            showMusicAppPickerDialog();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        // Pressing Home button re-fires onNewIntent on singleTask launcher
        String selectedPkg = mPrefs.getSelectedMusicPackage();
        if (selectedPkg != null && !selectedPkg.isEmpty() && mPrefs.isAutoLaunchEnabled()) {
            triggerMusicAppLaunch();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            ImmersiveModeHelper.applyImmersiveMode(this);
        }
    }


    private void handleBackAction() {
        String selectedPkg = mPrefs.getSelectedMusicPackage();
        if (selectedPkg != null && !selectedPkg.isEmpty()) {
            triggerMusicAppLaunch();
        } else {
            // If no music player chosen yet, show picker instead of leaving blank screen
            showMusicAppPickerDialog();
        }
    }

    private void triggerMusicAppLaunch() {
        String selectedPkg = mPrefs.getSelectedMusicPackage();
        if (selectedPkg == null || selectedPkg.isEmpty()) {
            showMusicAppPickerDialog();
            return;
        }

        boolean targetNowPlaying = mPrefs.isTargetNowPlayingEnabled();
        boolean launched = MusicAppManager.launchMusicApp(this, selectedPkg, targetNowPlaying);
        if (!launched) {
            // Package might have been uninstalled
            mPrefs.setSelectedMusicPackage(null, null);
            updateLauncherUi();
            showMusicAppPickerDialog();
        }
    }

    private void updateLauncherUi() {
        String selectedPkg = mPrefs.getSelectedMusicPackage();
        String selectedName = mPrefs.getSelectedMusicAppName();
        int accentColor = ThemeHelper.getAccentColor(this);
        int cornerRadius = getResources().getDimensionPixelSize(R.dimen.button_corner_radius);

        TextView tvLauncherStatus = findViewById(R.id.tv_launcher_status);
        if (tvLauncherStatus != null) {
            tvLauncherStatus.setTextColor(accentColor);
        }

        if (selectedPkg != null && !selectedPkg.isEmpty()) {
            mTvMusicName.setText(selectedName != null ? selectedName : selectedPkg);
            mTvMusicSubtitle.setText(getString(R.string.launching_music, selectedName != null ? selectedName : "Music"));
            mBtnLaunchMusic.setText(getString(R.string.open_now_playing));
            mBtnLaunchMusic.setVisibility(View.VISIBLE);
            ThemeHelper.applyAccentButton(mBtnLaunchMusic, accentColor, cornerRadius);
            mBtnSelectMusicApp.setText(getString(R.string.change_player));

            try {
                Drawable icon = getPackageManager().getApplicationIcon(selectedPkg);
                mIvMusicIcon.setImageDrawable(icon);
            } catch (PackageManager.NameNotFoundException e) {
                mIvMusicIcon.setImageResource(R.drawable.ic_music_note);
            }
        } else {
            mTvMusicName.setText(R.string.no_app_selected);
            mTvMusicSubtitle.setText(R.string.tap_to_select);
            mBtnLaunchMusic.setVisibility(View.GONE);
            mBtnSelectMusicApp.setText(R.string.select_music_app);
            ThemeHelper.applyAccentButton(mBtnSelectMusicApp, accentColor, cornerRadius);
            mIvMusicIcon.setImageResource(R.drawable.ic_music_note);
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
            updateLauncherUi();
            LauncherNotificationService.updateNotification(this);
            dialog.dismiss();
            triggerMusicAppLaunch();
        });

        rv.setAdapter(adapter);
        dialog.show();
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void openAppDrawer() {
        Intent intent = new Intent(this, AppDrawerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
