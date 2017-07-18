package com.formakidov.itechturvotestproject;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

class BottomSheetRecyclerView extends RecyclerView {
    private boolean interceptTouchEvents;

    public BottomSheetRecyclerView(@NonNull Context context) {
        super(context);
        init();
    }

    public BottomSheetRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BottomSheetRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return interceptTouchEvents;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        Log.d("logff", "TOUCH ========== R " + e.getY());
        return super.onTouchEvent(e);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.d("logff", "DISPATCH ======= R");
        return super.dispatchTouchEvent(ev);
    }

    public void setInterceptTouchEvents(boolean interceptTouchEvents) {
        this.interceptTouchEvents = interceptTouchEvents;
    }

}
