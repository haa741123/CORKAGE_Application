package com.example.test;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        TextView next = findViewById(R.id.textView2);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveMain();
            }
        });
    }

    // 메인 액티비티로 이동하는 메소드
    private void moveMain() {
        Intent intent = new Intent(Splash.this, Login.class);
        startActivity(intent);
        finish(); // 현재 액티비티 종료
    }
}
