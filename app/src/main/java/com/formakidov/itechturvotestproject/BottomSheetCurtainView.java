package com.formakidov.itechturvotestproject;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;


class BottomSheetCurtainView extends FrameLayout {

    private boolean interceptTouchEvents;

    public BottomSheetCurtainView(Context context) {
        super(context);
    }

    public BottomSheetCurtainView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BottomSheetCurtainView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!interceptTouchEvents) {
            requestDisallowInterceptTouchEvent(true);
            return super.onTouchEvent(event);
        } else {
            return true;
        }
    }

    public void setInterceptTouchEvents(boolean interceptTouchEvents) {
        this.interceptTouchEvents = interceptTouchEvents;
    }
}