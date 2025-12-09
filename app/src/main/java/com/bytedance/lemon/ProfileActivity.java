package com.bytedance.lemon;

import android.os.Bundle;
import android.transition.Fade;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.Observer;

import com.bumptech.glide.Glide;
import com.bytedance.lemon.recyclerview.entity.User;
import com.bytedance.lemon.recyclerview.repository.UserRepository;

public class ProfileActivity extends AppCompatActivity {

//    private UserAdapter adapter; // Adapter类型需要改为User

    private TextView tvUserName;

    private UserRepository userRepository;
    private long mUserId;

    private TextView mDescText;
    private EditText mDescEdit;
    private boolean isEditMode = false;
    private User mCurrentUser;

    private ImageView profile_avator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 启用窗口内容过渡
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        // 设置进入和退出动画
        getWindow().setEnterTransition(new Fade());
        getWindow().setExitTransition(new Fade());

        // 设置共享元素进入和退出动画
        getWindow().setSharedElementEnterTransition(TransitionInflater.from(this)
                .inflateTransition(android.R.transition.move));
        getWindow().setSharedElementExitTransition(TransitionInflater.from(this)
                .inflateTransition(android.R.transition.move));

        setContentView(R.layout.activity_profile);

        tvUserName = findViewById(R.id.profile_name);
        mDescText = findViewById(R.id.profile_desc_textView);
        mDescEdit = findViewById(R.id.profile_desc_editText);
        profile_avator = findViewById(R.id.profile_avatar);

        // 延迟设置共享元素过渡名称，等待布局加载完成
        postponeEnterTransition();


        mUserId = getIntent().getLongExtra("EXTRA_USER_ID", -1);


        // 设置共享元素过渡名称
        if (mUserId != -1) {
//            ImageView avatar = findViewById(R.id.profile_avatar);
            CardView cardView = findViewById(R.id.profile_info);

            String avatarTransitionName = "avatar_" + mUserId;
            String cardTransitionName = "card_" + mUserId;

            profile_avator.setTransitionName(avatarTransitionName);
            cardView.setTransitionName(cardTransitionName);
        }

        // 监听图片加载完成后再开始动画
        profile_avator.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        profile_avator.getViewTreeObserver().removeOnPreDrawListener(this);
                        startPostponedEnterTransition();
                        return true;
                    }
                }
        );




        //初始化userRespository
        userRepository = new UserRepository(getApplication());
        //
//        mUserId = getIntent().getLongExtra("EXTRA_USER_ID", -1);

        userRepository.getUserByIdLive(mUserId).observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                mCurrentUser = user;
                updateUI(user);
            }
        });

        setupSwipeToDismiss();

        setupClickListeners();
    }

    private void setupSwipeToDismiss() {
        // 获取根布局
        View rootView = findViewById(R.id.root_layout); // 需要在布局中添加根布局的id

        rootView.setOnTouchListener(new View.OnTouchListener() {
            private float startY;
            private boolean isDismissing = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startY = event.getRawY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        float currentY = event.getRawY();
                        float deltaY = currentY - startY;

                        // 如果是向下滑动
                        if (deltaY > 50 && !isDismissing) {
                            isDismissing = true;
                            supportFinishAfterTransition();
                            return true;
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        isDismissing = false;
                        break;
                }
                return false;
            }
        });
    }


    private void updateUI(User user) {


        tvUserName.setText(user.getName());
        mDescText.setText(user.getDescription());
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            Glide.with(this)
                    .load(user.getAvatarUrl())
                    .placeholder(R.drawable.avator_15)
                    .into(profile_avator);}

    }


    // 当点击“编辑”按钮时调用此方法
    private void toggleEditMode() {
        isEditMode = !isEditMode;

        if (isEditMode) {
            // 进入编辑模式
            mDescEdit.setText(mDescText.getText().toString());
            mDescText.setVisibility(View.GONE);
            mDescEdit.setVisibility(View.VISIBLE);
            mDescEdit.requestFocus();


        } else {
            // 退出编辑模式，保存数据到数据库
            String newDesc = mDescEdit.getText().toString().trim();

            // 1. 立即更新UI显示
            mDescText.setText(newDesc);
            mDescEdit.setVisibility(View.GONE);
            mDescText.setVisibility(View.VISIBLE);

            // 2. 保存到数据库
            if (userRepository != null && mUserId > 0) {
                userRepository.updateUserDesc(mUserId, newDesc);
                Toast.makeText(this, "备注已保存", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("ProfileActivity", "无法保存：Repository或UserID无效");
                Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
            }


        }
    }


    private void setupClickListeners() {
        // 个人信息
        findViewById(R.id.my_infomation).setOnClickListener(v -> {
            toggleEditMode();
        });

        // 我的收藏
        findViewById(R.id.my_stars).setOnClickListener(v -> {
            Toast.makeText(this, "我的收藏", Toast.LENGTH_SHORT).show();
        });

        // 浏览历史
        findViewById(R.id.my_history).setOnClickListener(v -> {
            Toast.makeText(this, "浏览历史", Toast.LENGTH_SHORT).show();
        });

        // 设置
        findViewById(R.id.my_setting).setOnClickListener(v -> {
            Toast.makeText(this, "设置", Toast.LENGTH_SHORT).show();
        });

        // 关于我们
        findViewById(R.id.my_about).setOnClickListener(v -> {
            Toast.makeText(this, "关于我们", Toast.LENGTH_SHORT).show();
        });

        // 意见反馈
        findViewById(R.id.my_feedback).setOnClickListener(v -> {
            Toast.makeText(this, "意见反馈", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.my_exit).setOnClickListener(v -> {

            String info = mCurrentUser.getName() + ": !!!" + mCurrentUser.getDescription();
            Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
            supportFinishAfterTransition();

            finish();
        });

    }


    // 同样处理返回键
//    @Override
//    public void onBackPressed() {
//        supportFinishAfterTransition();
//    }

}
