package project.listick.fakegps.UI;

import android.app.Activity;
import android.app.Dialog;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;


import project.listick.fakegps.R;

public class CaptchaActivity extends FragmentActivity {

    public static final String KEY_CAPTCHA_RESULT = "captcha_result";
    public static final String KEY_DATA = "data";

    public static final int ACTIVITY_REQUEST_CODE = 9;

    private Intent data;

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED, null);
        finish();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_captcha);

        UiModeManager ui = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);

        data = getIntent().getParcelableExtra(KEY_DATA);

        if (data == null) {
            data = new Intent();
        }

        WebView webView = findViewById(R.id.webview);

        webView.setBackgroundColor(Color.TRANSPARENT);

        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);

        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void onCaptchaPassed(String result) {
                finishActivity(data, result);
            }
        }, "CaptchaCallback");


        webView.loadUrl("http://littleangry.ru/lfg/verification.html?theme=" + ui.getNightMode());
    }

    private void finishActivity(Intent data, String tokenResult) {
        data.putExtra(CaptchaActivity.KEY_CAPTCHA_RESULT, tokenResult);
        setResult(RESULT_OK, data);
        finish();
    }

}
