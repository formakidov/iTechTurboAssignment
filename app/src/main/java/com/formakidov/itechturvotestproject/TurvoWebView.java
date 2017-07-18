package com.formakidov.itechturvotestproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TurvoWebView extends WebView {

    private int lastYTouchPoint;
    private boolean reachTop = true;
    private boolean reachBottom;
    private int scrollY;
    private boolean clampedY = true;

    public TurvoWebView(Context context) {
        super(context);
        init();
    }

    public TurvoWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TurvoWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        WebSettings settings = getSettings();
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setJavaScriptEnabled(true);
        setWebViewClient(new WebViewClient());
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        Log.d("logf", "onOverScrolled() called with: scrollY = [" + scrollY + "], clampedY = [" + clampedY + "]");
        this.scrollY = scrollY;
        this.clampedY = clampedY;
        if (clampedY) {
            reachTop = scrollY == 0;
            reachBottom = !reachTop;
        } else {
            reachTop = false;
            reachBottom = false;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.d("logff", "INTERCEPT === W " + shouldHandleTouchEvent(ev));
//        if (shouldHandleTouchEvent(ev)) {
//            return super.onInterceptTouchEvent(ev);
//        } else {
//            return false;
//        }


//        return shouldHandleTouchEvent(ev);

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean shouldHandleTouchEvent = shouldHandleTouchEvent(ev);
        Log.d("logff", "TOUCH ======= W " + shouldHandleTouchEvent);
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.d("logff", "DISPATCH ==== W " + shouldHandleTouchEvent(ev));
        return super.dispatchTouchEvent(ev);

//        if (shouldHandleTouchEvent(ev)) {
//            return super.dispatchTouchEvent(ev);
//        } else {
//            // TODO: 7/13/17 ????
//            return false;
//        }


        // if should handle
        // = true & return true - nothing,
        // = true & return false - scrolls parent
    }

    public boolean shouldHandleTouchEvent(MotionEvent ev) {
        int y = (int) ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d("logf", "Event:     DOWN");
                lastYTouchPoint = y;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                Log.d("logf", "Event:     CANCEL-UP");
                lastYTouchPoint = 0;
            case MotionEvent.ACTION_MOVE:
                if (lastYTouchPoint == 0) {
                    Log.d("logf", "Event:     MOVE 1");
                    lastYTouchPoint = y;
                } else {
                    if (lastYTouchPoint < y) {
                        // scroll to top
                        if (reachTop) {
                            Log.d("logf", "Event:     MOVE 2 /// " + scrollY + " / " + clampedY);
                            return false;
                        }
                    } else if (lastYTouchPoint > y) {
                        // scroll to bottom
                        if (reachBottom) {
                            Log.d("logf", "Event:     MOVE 3 /// " + scrollY + " / " + clampedY);
                            return false;
                        }
                    }
                }
                break;
        }
        return true;
    }
}
