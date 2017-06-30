package com.formakidov.itechturvotestproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TurvoLendingWebView extends WebView {
    private boolean gesturesEnabled;

    public TurvoLendingWebView(Context context) {
        super(context);
        init();
    }

    public TurvoLendingWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TurvoLendingWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        WebSettings settings = getSettings();
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setJavaScriptEnabled(true);
        setWebViewClient(new WebViewClient());
        loadUrl("http://www.google.com");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gesturesEnabled) {
            requestDisallowInterceptTouchEvent(true);
            return super.onTouchEvent(event);
        } else {
            return true;
        }
    }

    public void setGesturesEnabled(boolean gesturesEnabled) {
        this.gesturesEnabled = gesturesEnabled;
    }
}
