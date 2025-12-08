package com.bytedance.lemon.recyclerview.viewholder;

import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.lemon.R;

/**
 * create by WUzejian on 2025/11/17
 */
public class ImageViewHolder extends RecyclerView.ViewHolder {
    public ImageView imageView;

    public ImageViewHolder(View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.imgView);
    }
}
