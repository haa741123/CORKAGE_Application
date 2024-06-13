package com.example.test;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class PageOne extends AppCompatActivity {

    private static final String TAG = "PageOne";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_two_layout);

        Button next = findViewById(R.id.textView2);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Button clicked!");
                moveMain();
            }
        });
    }

    // 메인 액티비티로 이동하는 메소드
    private void moveMain() {
        Intent intent = new Intent(PageOne.this, Login.class);
        startActivity(intent);
        finish(); // 현재 액티비티 종료
    }
}
