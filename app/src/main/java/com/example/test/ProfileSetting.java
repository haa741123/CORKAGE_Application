package com.example.test;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ProfileSetting extends AppCompatActivity {
    WebView webview;
    TextView textview;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profilesetting);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // 여기에서 navigation_notifications 항목을 선택된 상태로 설정
        bottomNavigationView.setSelectedItemId(R.id.navigation_notifications);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                Intent intent = new Intent(ProfileSetting.this, MainActivity.class);
                startActivity(intent);
                return true;
            }
            if (item.getItemId() == R.id.navigation_notifications) {
                Intent intent = new Intent(ProfileSetting.this, Mypage.class);
                startActivity(intent);
                return true;
            }
            return false;
        });


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webview.canGoBack()) {
            webview.goBack();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            new AlertDialog.Builder(this)
                    .setTitle("프로그램 종료")
                    .setMessage("앱을 종료하시겠습니까?")
                    .setPositiveButton("예", (dialog, which) -> android.os.Process.killProcess(android.os.Process.myPid()))
                    .setNegativeButton("아니오", null)
                    .show();
        }
        return super.onKeyDown(keyCode, event);
    }
}
