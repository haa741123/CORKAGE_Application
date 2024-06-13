package com.example.test;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

public class Splash extends AppCompatActivity {

    private static final int SPLASH_DELAY = 3000; // 3초

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        // 3초 뒤에 ExplainCard 액티비티로 이동
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                moveMain();
            }
        }, SPLASH_DELAY);
    }

    // 메인 액티비티로 이동하는 메소드
    private void moveMain() {
        Intent intent = new Intent(Splash.this, ExplainCard.class);
        startActivity(intent);
        finish(); // 현재 액티비티 종료
    }
}
