package com.formakidov.itechturvotestproject;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class BottomSheetView extends FrameLayout {
    private boolean contentTouchEventsEnabled;

    public BottomSheetView(@NonNull Context context) {
        super(context);
    }

    public BottomSheetView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BottomSheetView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        if (ev.getAction() == MotionEvent.ACTION_DOWN || ev.getAction() == MotionEvent.ACTION_MOVE || ev.getAction() == MotionEvent.ACTION_UP) {
//            Rect contentRect = new Rect();
//            View content = getChildAt(0);
//            content.getGlobalVisibleRect(contentRect);
//            Log.d("logf", "BottomSheetView onInterceptTouchEvent");
//            if (contentRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
//                if (contentTouchEventsEnabled) {
//                    Log.d("logf", "BottomSheetView contentTouchEventsEnabled: true ");
//                    content.dispatchTouchEvent(ev);
//                } else {
//                    Log.d("logf", "BottomSheetView contentTouchEventsEnabled: false ");
//                }
//                return false;
//            }
//        }
//        return super.onInterceptTouchEvent(ev);
//    }

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        if (ev.getAction() == MotionEvent.ACTION_DOWN || ev.getAction() == MotionEvent.ACTION_MOVE) {
//            Rect childRect = new Rect();
//            getChildAt(0).getGlobalVisibleRect(childRect);
//            if (childRect.contains((int) ev.getRawX(), (int) ev.getRawY()) && contentTouchEventsEnabled) {
//                Log.d("logf", "BottomSheetView dispatchTouchEvent: true ");
//                return false;
//            }
//        }
//        return super.dispatchTouchEvent(ev);
//    }

    public void setContentTouchEventsEnabled(boolean contentTouchEventsEnabled) {
        this.contentTouchEventsEnabled = contentTouchEventsEnabled;
    }
}
