package com.example.test;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class MainActivity extends AppCompatActivity {
    WebView webview;
    TextView textview;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ActionBar 숨기기
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        webview = findViewById(R.id.activity_main_web);
        textview = findViewById(R.id.textView);

        // 관심지역을 설정해주세요 클릭 시 바텀시트 띄우기
        TextView textView3 = findViewById(R.id.location);
        textView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyBottomSheetDialogFragment bottomSheetDialogFragment = new MyBottomSheetDialogFragment();
                bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            setWebView();
        }
    }

    private void setWebView() {
        WebSettings websettings = webview.getSettings();
        websettings.setJavaScriptEnabled(true);

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("tel:")) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                    startActivity(intent);
                    view.reload();
                    return true;
                }

                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                webview.setVisibility(View.GONE);
                textview.setVisibility(View.VISIBLE);
            }
        });

        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("알림")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("알림")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new AlertDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                result.cancel();
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
                return true;
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                super.onGeolocationPermissionsShowPrompt(origin, callback);
                callback.invoke(origin, true, false);
            }
        });
        webview.loadUrl("http://121.142.17.86:80");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webview.canGoBack()) {
                webview.goBack();
                return false;
            }
        }

        if (keyCode == KeyEvent.KEYCODE_BACK && !webview.canGoBack()) {
            new AlertDialog.Builder(this)
                    .setTitle("프로그램 종료")
                    .setMessage("모두의 잔을 종료하시겠습니까?")
                    .setPositiveButton("예", new AlertDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    })
                    .setNegativeButton("아니오", null)
                    .show();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0) {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        // 위치 권한 팝업을 띄움
                        new AlertDialog.Builder(this).setMessage("위치 권한을 허용해주세요")
                                .setPositiveButton("종료", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                })
                                .setNegativeButton("권한 설정", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                                .setData(Uri.parse("package:" + getApplicationContext().getPackageName()))
                                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        setWebView();
                                    }
                                })
                                .setCancelable(false).show();
                    } else {
                        setWebView();
                    }
                }
            }
        }
    }

    public static class MyBottomSheetDialogFragment extends BottomSheetDialogFragment {
        private boolean waitingForDismissAllowingStateLoss;

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            return new BottomSheetDialog(requireContext(), getTheme());
        }

        @Override
        public void dismiss() {
            if (!tryDismissWithAnimation(false)) {
                super.dismiss();
            }
        }

        @Override
        public void dismissAllowingStateLoss() {
            if (!tryDismissWithAnimation(true)) {
                super.dismissAllowingStateLoss();
            }
        }

        private boolean tryDismissWithAnimation(boolean allowingStateLoss) {
            Dialog baseDialog = getDialog();
            if (baseDialog instanceof BottomSheetDialog) {
                BottomSheetDialog dialog = (BottomSheetDialog) baseDialog;
                BottomSheetBehavior<?> behavior = dialog.getBehavior();
                if (behavior.isHideable() && dialog.getDismissWithAnimation()) {
                    dismissWithAnimation(behavior, allowingStateLoss);
                    return true;
                }
            }

            return false;
        }

        private void dismissWithAnimation(@NonNull BottomSheetBehavior<?> behavior, boolean allowingStateLoss) {
            waitingForDismissAllowingStateLoss = allowingStateLoss;

            if (behavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                dismissAfterAnimation();
            } else {
                behavior.addBottomSheetCallback(new BottomSheetDismissCallback());
                behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        }

        private void dismissAfterAnimation() {
            if (waitingForDismissAllowingStateLoss) {
                super.dismissAllowingStateLoss();
            } else {
                super.dismiss();
            }
        }

        private class BottomSheetDismissCallback extends BottomSheetBehavior.BottomSheetCallback {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    dismissAfterAnimation();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        }
    }
}
