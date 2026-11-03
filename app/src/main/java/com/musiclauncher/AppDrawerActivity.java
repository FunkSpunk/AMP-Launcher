package com.musiclauncher;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.musiclauncher.adapter.AppAdapter;
import com.musiclauncher.utils.ImmersiveModeHelper;
import com.musiclauncher.utils.ThemeHelper;

/**
 * App Drawer Activity allowing users to launch any installed app on their device.
 */
public class AppDrawerActivity extends AppCompatActivity {

    private TextView mTvEmpty;
    private AppAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_drawer);

        EditText etSearch = findViewById(R.id.et_search);
        RecyclerView rvApps = findViewById(R.id.rv_apps);
        mTvEmpty = findViewById(R.id.tv_empty);

        int spanCount = getResources().getConfiguration().screenWidthDp >= 600 ? 5 : 4;
        rvApps.setLayoutManager(new GridLayoutManager(this, spanCount));
        mAdapter = new AppAdapter(this);
        rvApps.setAdapter(mAdapter);

        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                mTvEmpty.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mAdapter.filter(s != null ? s.toString() : "");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ImmersiveModeHelper.applyImmersiveMode(this);
    }
}
