// RewardDialogFragment.java
package com.bytedance.lemon;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;

public class RewardDialogFragment extends DialogFragment {

    private static final String ARG_MESSAGE = "message";

    public static RewardDialogFragment newInstance(String message) {
        RewardDialogFragment fragment = new RewardDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        // 加载自定义布局
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_reward, null);

        // 初始化视图
        ImageView ivRewardIcon = view.findViewById(R.id.iv_reward_icon);
        TextView tvRewardTitle = view.findViewById(R.id.tv_reward_title);
        TextView tvRewardDesc = view.findViewById(R.id.tv_reward_desc);
        Button btnClaim = view.findViewById(R.id.btn_claim);
        Button btnClose = view.findViewById(R.id.btn_close);

        // 设置内容
        String message = getArguments() != null ? getArguments().getString(ARG_MESSAGE) : "";
        tvRewardTitle.setText("恭喜获得奖励！");
        tvRewardDesc.setText(message);

        // 加载图标
        Glide.with(this)
                .load(R.drawable.ic_gift)
                .into(ivRewardIcon);

        // 设置按钮点击事件
        btnClaim.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "奖励已发放到账户", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        btnClose.setOnClickListener(v -> dismiss());

        builder.setView(view);
        return builder.create();
    }
}