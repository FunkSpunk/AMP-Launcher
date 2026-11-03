package com.musiclauncher.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.musiclauncher.R;
import com.musiclauncher.utils.MusicAppManager.MusicAppInfo;

import java.util.List;

/**
 * Adapter for displaying the list of music players in the picker dialog.
 */
public class MusicAppAdapter extends RecyclerView.Adapter<MusicAppAdapter.ViewHolder> {

    public interface OnMusicAppSelectedListener {
        void onMusicAppSelected(MusicAppInfo appInfo);
    }

    private final List<MusicAppInfo> mItems;
    private final String mCurrentlySelectedPackage;
    private final OnMusicAppSelectedListener mListener;

    public MusicAppAdapter(List<MusicAppInfo> items, String currentlySelectedPackage, OnMusicAppSelectedListener listener) {
        this.mItems = items;
        this.mCurrentlySelectedPackage = currentlySelectedPackage;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_music_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MusicAppInfo item = mItems.get(position);
        holder.tvAppName.setText(item.appName);
        holder.tvPackageName.setText(item.packageName);
        holder.ivIcon.setImageDrawable(item.icon);

        boolean isSelected = java.util.Objects.equals(item.packageName, mCurrentlySelectedPackage);
        holder.ivCheck.setVisibility(isSelected ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onMusicAppSelected(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivIcon;
        final TextView tvAppName;
        final TextView tvPackageName;
        final ImageView ivCheck;

        ViewHolder(View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_app_icon);
            tvAppName = itemView.findViewById(R.id.tv_app_name);
            tvPackageName = itemView.findViewById(R.id.tv_package_name);
            ivCheck = itemView.findViewById(R.id.iv_selected_check);
        }
    }
}
