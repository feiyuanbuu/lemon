package com.bytedance.lemon;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

import android.widget.Button;

import com.bytedance.lemon.recyclerview.RecyclerViewActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int TOTAL_TIME = 5000; // 总时间5秒
    private static final int INTERVAL = 1000; // 间隔1秒

    private Button btnSkip;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        btnSkip = findViewById(R.id.btn_skip);

        // 设置跳过按钮点击事件
        btnSkip.setOnClickListener(v -> {
//            jumpToMain();
            jumpToRecyclerViewActivity();
        });


        // 倒计时定时器
        countDownTimer = new CountDownTimer(TOTAL_TIME, INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                // 更新跳过按钮文本
                btnSkip.setText("跳过 " + (millisUntilFinished / 1000) + "s");
            }

            @Override
            public void onFinish() {
                //jumpToMain()
                jumpToRecyclerViewActivity();
            }
        }.start();
    }

    private void jumpToRecyclerViewActivity() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        Intent intent = new Intent(this, RecyclerViewActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }


}

