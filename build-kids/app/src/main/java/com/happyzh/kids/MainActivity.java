package com.happyzh.kids;
import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.webkit.*;
import android.content.pm.ActivityInfo;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {
    private WebView wv;
    private File downloadDir;
    private ConcurrentHashMap<String, DownloadTask> downloads = new ConcurrentHashMap<>();

    static class DownloadTask {
        volatile boolean active;
        volatile int progress; // -1=error, 0-100
        String localPath;
    }

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        downloadDir = new File(getExternalFilesDir(null), "videos");
        downloadDir.mkdirs();

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
    public String isDownloaded(String filename) {
        File f = new File(downloadDir, filename + ".mp4");
        return f.exists() ? "file://" + f.getAbsolutePath() : "";
    }

    @android.webkit.JavascriptInterface
    public void downloadVideo(final String url, final String filename) {
        final File outFile = new File(downloadDir, filename + ".mp4");
        if (outFile.exists()) return;

        DownloadTask existing = downloads.get(filename);
        if (existing != null && existing.active) return;

        final DownloadTask task = new DownloadTask();
        task.active = true;
        task.progress = 0;
        task.localPath = outFile.getAbsolutePath();
        downloads.put(filename, task);

        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(60000);
                    conn.setRequestProperty("User-Agent", "KidsApp/1.0");
                    int total = conn.getContentLength();
                    InputStream in = conn.getInputStream();
                    File tmp = new File(outFile.getAbsolutePath() + ".tmp");
                    FileOutputStream out = new FileOutputStream(tmp);
                    byte[] buf = new byte[65536];
                    int read, downloaded = 0;
                    while ((read = in.read(buf)) != -1) {
                        out.write(buf, 0, read);
                        downloaded += read;
                        if (total > 0) task.progress = (int) (downloaded * 100L / total);
                    }
                    out.close();
                    in.close();
                    conn.disconnect();
                    tmp.renameTo(outFile);
                    task.progress = 100;
                } catch (Exception e) {
                    outFile.delete();
                    new File(outFile.getAbsolutePath() + ".tmp").delete();
                    task.progress = -1;
                }
                task.active = false;
            }
        }).start();
    }

    @android.webkit.JavascriptInterface
    public String getDownloadState(String filename) {
        DownloadTask t = downloads.get(filename);
        if (t == null) {
            File f = new File(downloadDir, filename + ".mp4");
            return f.exists() ? "{\"state\":\"done\"}" : "{\"state\":\"none\"}";
        }
        if (!t.active) {
            downloads.remove(filename);
            return t.progress < 0 ? "{\"state\":\"error\"}" : "{\"state\":\"done\"}";
        }
        return "{\"state\":\"downloading\",\"progress\":" + t.progress + "}";
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
