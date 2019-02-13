package io.github.formular_team.formular;

import android.graphics.Color;
import android.os.Bundle;

import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewImpl;
import org.apache.cordova.engine.SystemWebView;
import org.apache.cordova.engine.SystemWebViewEngine;

public class MainActivity extends CordovaActivity  {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadUrl(launchUrl);
    }

    @Override
    protected CordovaWebView makeWebView() {
        SystemWebView webView = findViewById(R.id.web_view);
        webView.setBackgroundColor(Color.TRANSPARENT);
        return new CordovaWebViewImpl(new SystemWebViewEngine(webView));
    }

    @Override
    protected void createViews() {}
}
