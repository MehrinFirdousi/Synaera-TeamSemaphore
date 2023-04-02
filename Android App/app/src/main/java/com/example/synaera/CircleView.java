package com.example.synaera;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.core.content.ContextCompat;

import java.util.Objects;

public class CircleView extends View {
    private Paint mPaint;
    private AnimatorSet mAnimatorSet = new AnimatorSet();

    private float mMinRadius;
    private float mMaxRadius;
    private float mCurrentRadius;
    private float mCurrentStroke;
    private float mMinStroke;
    private float mMaxStroke;

    public CircleView(Context context) {
        super(context);
        init();
    }

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public float getmMinRadius() {
        return mMinRadius;
    }

    public float getmMaxRadius() {
        return mMaxRadius;
    }

    public float getmMinStroke() {
        return mMinStroke;
    }

    public float getmMaxStroke() {
        return mMaxStroke;
    }

    private void init() {
        mMinRadius = dpToPx(getContext(), 40);
        mMinStroke = 15;
        mCurrentRadius = mMinRadius;
        mCurrentStroke = mMinStroke;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mMinStroke);
        mPaint.setStyle(Paint.Style.STROKE);
//        mPaint.setColor(Color.rgb(255, 0, 0));
        int color = ContextCompat.getColor(this.getContext(), R.color.recordRedTrans);
        mPaint.setColor(color);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldWidth, int oldHeight) {
        super.onSizeChanged(w, h, oldWidth, oldHeight);
        mMaxRadius = (float) (Math.min(w, h) / 2.2);
        mMaxStroke = 60;
    }

    public int dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = Objects.requireNonNull(context).getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        float radius = Math.max(mCurrentRadius, mMinRadius);
        float stroke = Math.max(mCurrentStroke, mMinStroke);
        mPaint.setStrokeWidth(stroke);
        canvas.drawCircle(width / 2, height / 2, radius, mPaint);
    }

    public void animateRadius(float radius, float stroke) {

        if (stroke > mMaxStroke) {
            stroke = mMaxStroke;
        } else if (stroke < mMinStroke) {
            stroke = mMinStroke;
        }


        if (mAnimatorSet.isRunning()) {
            mAnimatorSet.cancel();
        }
        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.play(ObjectAnimator.ofFloat(this, "CurrentRadius", getCurrentRadius(), (float) (radius - (stroke / 2.2))))
                .with(ObjectAnimator.ofFloat(this, "CurrentStroke", getCurrentStroke(), stroke));
        mAnimatorSet.setDuration(80);
        mAnimatorSet.setInterpolator(new DecelerateInterpolator());
        mAnimatorSet.start();
        invalidate();
    }

    public float getCurrentRadius() {
        return mCurrentRadius;
    }

    /**
     * required this method to set ObjectAnimator
     *
     * @param currentRadius current radius
     */
    public void setCurrentRadius(float currentRadius) {
        mCurrentRadius = currentRadius;
        invalidate();
    }

    public float getCurrentStroke() {
        return mCurrentStroke;
    }

    public void setCurrentStroke(float mCurrentStroke) {
        this.mCurrentStroke = mCurrentStroke;
        invalidate();
    }

    public void endAnimation() {
        if (mAnimatorSet != null) mAnimatorSet.end();
    }
}
