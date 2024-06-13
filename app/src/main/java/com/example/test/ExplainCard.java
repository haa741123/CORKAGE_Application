package com.example.test;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.viewpager2.widget.ViewPager2;

public class ExplainCard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.explain_card);

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        int[] layouts = {R.layout.page_one_layout, R.layout.page_two_layout};

        View.OnClickListener pageListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveMain();
            }
        };

        ImageAdapter adapter = new ImageAdapter(layouts, pageListener);
        viewPager.setAdapter(adapter);
    }

    private void moveMain() {
        Intent intent = new Intent(ExplainCard.this, Login.class);
        startActivity(intent);
        finish();
    }
}
