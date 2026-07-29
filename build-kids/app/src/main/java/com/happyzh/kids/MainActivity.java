package com.happyzh.kids;
import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.webkit.*;
import android.content.pm.ActivityInfo;
public class MainActivity extends Activity {
    private WebView wv;
    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        wv = new WebView(this);
        WebSettings ws = wv.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setAllowFileAccess(true);
        ws.setAllowFileAccessFromFileURLs(true);
        ws.setAllowUniversalAccessFromFileURLs(true);
        ws.setMediaPlaybackRequiresUserGesture(false);
        ws.setUseWideViewPort(true);
        ws.setLoadWithOverviewMode(true);
        ws.setSupportZoom(false);
        wv.setWebViewClient(new WebViewClient());
        wv.setWebChromeClient(new WebChromeClient());
        wv.loadUrl("file:///android_asset/index.html");
        setContentView(wv);
        wv.addJavascriptInterface(this, "Android");
    }
    @android.webkit.JavascriptInterface
    public void requestLandscape() {
        runOnUiThread(new Runnable() {
            @Override public void run() { setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE); }
        });
    }
    @android.webkit.JavascriptInterface
    public void requestPortrait() {
        runOnUiThread(new Runnable() {
            @Override public void run() { setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); }
        });
    }
}
