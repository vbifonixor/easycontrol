package com.daitj.easycontrolfork.app.client.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MyViewForSmallView extends FrameLayout {

  private MyFunctionMotionEvent onTouchHandle;
  private MyFunctionGenericMotionEvent onGenericMotionHandle;
  private MyFunctionKeyEvent onKeyHandle;

  public MyViewForSmallView(@NonNull Context context) {
    super(context);
  }

  public MyViewForSmallView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public MyViewForSmallView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public MyViewForSmallView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    if (onTouchHandle != null) onTouchHandle.run(ev);
    return super.dispatchTouchEvent(ev);
  }

  @Override
  public boolean dispatchGenericMotionEvent(MotionEvent ev) {
    if (onGenericMotionHandle != null && onGenericMotionHandle.run(ev)) return true;
    return super.dispatchGenericMotionEvent(ev);
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    if (onKeyHandle != null && onKeyHandle.run(event)) return true;
    return super.dispatchKeyEvent(event);
  }

  public void setOnTouchHandle(MyFunctionMotionEvent handle) {
    onTouchHandle = handle;
  }

  public void setOnGenericMotionHandle(MyFunctionGenericMotionEvent handle) {
    onGenericMotionHandle = handle;
  }

  public void setOnKeyHandle(MyFunctionKeyEvent handle) {
    onKeyHandle = handle;
  }

  public interface MyFunctionMotionEvent {
    void run(MotionEvent event);
  }

  public interface MyFunctionGenericMotionEvent {
    boolean run(MotionEvent event);
  }

  public interface MyFunctionKeyEvent {
    boolean run(KeyEvent event);
  }
}
