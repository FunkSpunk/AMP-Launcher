package com.musiclauncher.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.musiclauncher.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adapter for displaying all installed applications in the App Drawer grid.
 */
public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {

    public static class AppItem implements Comparable<AppItem> {
        public final String packageName;
        public final String className;
        public final String label;
        public final Drawable icon;

        public AppItem(String packageName, String className, String label, Drawable icon) {
            this.packageName = packageName;
            this.className = className;
            this.label = label;
            this.icon = icon;
        }

        @Override
        public int compareTo(AppItem o) {
            return this.label.compareToIgnoreCase(o.label);
        }
    }

    private final Context mContext;
    private final List<AppItem> mOriginalList = new ArrayList<>();
    private final List<AppItem> mFilteredList = new ArrayList<>();

    public AppAdapter(Context context) {
        this.mContext = context;
        loadApps();
    }

    public void loadApps() {
        mOriginalList.clear();
        PackageManager pm = mContext.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolves = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo ri : resolves) {
            if (ri.activityInfo != null) {
                String pkg = ri.activityInfo.packageName;
                if (java.util.Objects.equals(pkg, mContext.getPackageName())) continue; // omit self

                String label = ri.loadLabel(pm).toString();
                Drawable icon = ri.loadIcon(pm);
                mOriginalList.add(new AppItem(pkg, ri.activityInfo.name, label, icon));
            }
        }
        Collections.sort(mOriginalList);
        filter("");
    }

    @android.annotation.SuppressLint("NotifyDataSetChanged")
    public void filter(String query) {
        mFilteredList.clear();
        if (query == null || query.trim().isEmpty()) {
            mFilteredList.addAll(mOriginalList);
        } else {
            String q = query.toLowerCase(java.util.Locale.ROOT).trim();
            for (AppItem item : mOriginalList) {
                if (item.label.toLowerCase(java.util.Locale.ROOT).contains(q) || item.packageName.toLowerCase(java.util.Locale.ROOT).contains(q)) {
                    mFilteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppItem item = mFilteredList.get(position);
        holder.tvName.setText(item.label);
        holder.ivIcon.setImageDrawable(item.icon);

        holder.itemView.setOnClickListener(v -> {
            Intent launchIntent = mContext.getPackageManager().getLaunchIntentForPackage(item.packageName);
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(launchIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFilteredList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivIcon;
        final TextView tvName;

        ViewHolder(View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_grid_app_icon);
            tvName = itemView.findViewById(R.id.tv_grid_app_name);
        }
    }
}
