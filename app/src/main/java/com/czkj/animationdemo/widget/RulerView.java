package com.czkj.animationdemo.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.czkj.animationdemo.util.DensityUtil;

import java.util.Locale;

/**
 * Created by whlin on 2017/10/16.
 *
 */

public class RulerView extends View {

    //最大跨度
    private static final double MAX_LENGTH = 3.5;
    //滚动阀值
    private static final int SCROLLABLE_VALUE = 1000;
    //刻度线宽度
    private static final float PAINT_WIDTH = 2.5f;
    //短线长度
    private static final int SHORT_LINE_HEIGHT = 20;
    //长线长度
    private static final int LONG_LINE_HEIGHT = 45;
    //目标线宽度
    private static final int TARGET_BAR_DEFAULT_WIDTH = 5;
    //目标线长度
    private static final int TARGET_BAR_DEFAULT_HEIGHT = 50;
    //默认最小值
    private static final int DEFAULT_MIN_VALUE = Integer.MIN_VALUE;
    //默认最大值
    private static final int DEFAULT_MAX_VALUE = Integer.MAX_VALUE;
    //目标线默认颜色
    private static final @ColorInt int TARGET_DEFAULT_COLOR = Color.parseColor("#4ABB73");
    //短线默认颜色
    private static final @ColorInt int SHORT_LINE_DEFAULT_COLOR = Color.parseColor("#E5E8E5");
    //长线默认颜色
    private static final @ColorInt int LONG_LINE_DEFAULT_COLOR = Color.parseColor("#DCDFDC");
    //字体默认颜色
    private static final @ColorInt int TEXT_DEFAULT_COLOR = Color.parseColor("#333333");
    //校正时,动画时长
    private static final long LOCATION_ANIMATION_DURATION = 150;
    //目标值
    private double mValue;
    //起始值
    private double mStartValue;
    //结束值
    private double mEndValue;
    //第一个划线的值
    private double mStartIndexValue;
    //每0.1刻度的长度
    private double mScaleValue;
    //短线
    private Paint mShortPaint;
    //长线
    private Paint mLongPaint;
    //目标线
    private Paint mTargetPaint;
    //字体画笔
    private Paint mTextPaint;
    //触摸移动值
    private float mMoveX;
    //滚动
    private boolean isFling;
    //按下时间
    private long mDownTime;
    //按下的x值
    private float mTmpX;
    //是否超出边界部分不绘制,默认绘制
    private boolean mIgnore;
    //最小值
    private float mMinValue = DEFAULT_MIN_VALUE;
    //最大值
    private float mMaxValue = DEFAULT_MAX_VALUE;
    //字体大小
    private int mTextSize;
    //目标线宽度
    private int mTargetBarWidth;
    //目标线长度
    private int mTargetBarHeight;
    //View的宽高
    private int mWidth, mHeight;
    //数值变化监听器
    private OnValueChangeListener mOnValueChangeListener;

    public void setOnValueChangeListener(OnValueChangeListener mOnValueChangeListener) {
        this.mOnValueChangeListener = mOnValueChangeListener;
    }

    //数值变化监听器
    public interface OnValueChangeListener {
        void onValueChange(double value);
    }

    public RulerView(Context context) {
        super(context);
        init(context);
    }

    public RulerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RulerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;

        mScaleValue = mWidth / (MAX_LENGTH * 10d);
        calcStartIndexValue();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        double startIndex = mStartIndexValue;

        while(startIndex <= mEndValue) {
            startIndex = Double.parseDouble(String.format(Locale.getDefault(), "%.1f", startIndex));
            //超出边界部分是否忽略
            if (mIgnore && (startIndex < mMinValue || startIndex > mMaxValue)) {
                startIndex += 0.1d;
                continue;
            }
            long longVal = (long) (startIndex * 10);
            //计算出x坐标
            int xIndex = (int) ((startIndex - mStartValue) / 0.1d * mScaleValue - (PAINT_WIDTH / 2));
            if (longVal % 10 != 0) {
                //短线
                drawShortLine(canvas, xIndex);
            } else {
                //长线
                drawLongLine(canvas, xIndex, longVal / 10);
            }
            startIndex += 0.1d;
        }
        //绘制目标线
        canvas.drawLine(mWidth / 2, 0, mWidth / 2, mTargetBarHeight, mTargetPaint);
    }

    private void drawLongLine(Canvas canvas, int xIndex, long value) {
        canvas.drawLine(xIndex, 0, xIndex, DensityUtil.dip2px(getContext(), LONG_LINE_HEIGHT), mLongPaint);
        canvas.drawText(String.format(Locale.getDefault(), "%d", value), xIndex, DensityUtil.dip2px(getContext(), LONG_LINE_HEIGHT) + mTextSize * 2, mTextPaint);
    }

    private void drawShortLine(Canvas canvas, int xIndex) {
        canvas.drawLine(xIndex, 0, xIndex, DensityUtil.dip2px(getContext(), SHORT_LINE_HEIGHT), mShortPaint);
    }

    private void init(Context context) {
        setClickable(true);
        mTextSize = DensityUtil.dip2px(context, 16);
        mTargetBarWidth = DensityUtil.dip2px(context, TARGET_BAR_DEFAULT_WIDTH);
        mTargetBarHeight = DensityUtil.dip2px(context, TARGET_BAR_DEFAULT_HEIGHT);

        mShortPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShortPaint.setColor(SHORT_LINE_DEFAULT_COLOR);
        mShortPaint.setStrokeWidth(DensityUtil.dip2px(context, PAINT_WIDTH));

        mLongPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLongPaint.setColor(LONG_LINE_DEFAULT_COLOR);
        mLongPaint.setStrokeWidth(DensityUtil.dip2px(context, PAINT_WIDTH));

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(TEXT_DEFAULT_COLOR);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mTargetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTargetPaint.setStrokeWidth(mTargetBarWidth);
        mTargetPaint.setColor(TARGET_DEFAULT_COLOR);
        mTargetPaint.setStrokeCap(Paint.Cap.ROUND);

        calcStartIndexValue();
    }

    public double getValue() {
        return mValue;
    }

    public void setValue(double value) {
        this.mValue = value;
        //控制最小边界
        if (value <= mMinValue) {
            this.mValue = mMinValue;
        }
        //控制最大边界
        if (value >= mMaxValue) {
            this.mValue = mMaxValue;
        }

        calcStartIndexValue();
        if (mOnValueChangeListener != null) {
            mOnValueChangeListener.onValueChange(mValue);
        }
    }

    private void calcStartIndexValue() {
        //计算起始值
        mStartValue = mValue - MAX_LENGTH / 2;
        //计算结束值
        mEndValue = mStartValue + MAX_LENGTH;
        //计算第一个刻度值
        mStartIndexValue = mStartValue * 100d;
        if (mStartIndexValue % 10 != 0) {
            int tmp = (int) (mStartIndexValue % 10);
            mStartIndexValue = mStartIndexValue + (10 - tmp);
        }
        mStartIndexValue /= 100d;
    }

    /**
     * 自动滚动的Runnable
     */
    private AutoFlingRunnable mFlingRunnable;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mMoveX = event.getX();
                mTmpX = mMoveX;
                mDownTime = System.currentTimeMillis();
                //停止滚动
                if (isFling) {
                    isFling = false;
                    removeCallbacks(mFlingRunnable);
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float delta = mMoveX - x ;
                //计算修改量
                double changeVal = (delta / mScaleValue / 10);
                //修改值
                setValue(mValue + changeVal);
                //重置
                postInvalidate();
                mMoveX = x;
                break;
            case MotionEvent.ACTION_UP:
                //计算单位时间内滑动的值
                float valuePerSecond = (mTmpX - mMoveX) * 1000 / (System.currentTimeMillis() - mDownTime);
                //如果超过一定值,则自动滚动
                if (Math.abs(valuePerSecond) > 1000 && !isFling) {
                    post(mFlingRunnable = new AutoFlingRunnable(valuePerSecond));
                    return true;
                } else {
                    relocation();
                }
                break;
        }

        return super.dispatchTouchEvent(event);
    }

    //校正,保证最小刻度为0.1
    private void relocation() {
        double value = mValue;
        //先取出第二位小数
        int last = ((int)(value * 100)) % 10;
        //刚好0.1精度,结束
        if (last == 0) {
            return;
        }
        //四舍五入
        if (last < 5) {
            value = (value * 100 - last) / 100;
        } else {
            value = (value * 100 + (10 - last)) / 100;
        }

        ValueAnimator animator = ValueAnimator.ofFloat((float)mValue, (float)value);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(LOCATION_ANIMATION_DURATION);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (float) valueAnimator.getAnimatedValue();
                setValue(value);
                postInvalidate();
            }
        });
        animator.start();
    }


    private class AutoFlingRunnable implements Runnable {

        private float valuePerSecond;

        AutoFlingRunnable(float velocity) {
            this.valuePerSecond = velocity;
        }

        @Override
        public void run() {
            // 如果小于20,则停止
            if ((int) Math.abs(valuePerSecond) < 20) {
                isFling = false;
                relocation();
                return;
            }

            //如果等于最小值 || 最大值,停止
            if (mValue == mMinValue || mValue == mMaxValue) {
                isFling = false;
                relocation();
                return;
            }

            isFling = true;
            double changeVal = (valuePerSecond / mScaleValue / 10 / 30);
            setValue(mValue + changeVal);
            // 逐渐减小这个值
            valuePerSecond /= 1.0666F;
            postDelayed(this, 30);
            postInvalidate();
        }
    }

    public float getMinValue() {
        return mMinValue;
    }

    public void setMinValue(float minValue) {

        if (minValue > mMaxValue - MAX_LENGTH) {
            return;
        }
        this.mMinValue = minValue;
        if (mValue < minValue) {
            mValue = minValue;
        }
    }

    public float getMaxValue() {
        return mMaxValue;
    }

    public void setMaxValue(float maxValue) {

        if (maxValue < mMinValue + MAX_LENGTH) {
            return;
        }

        this.mMaxValue = maxValue;
        if (mValue > mMaxValue) {
            mValue = mMaxValue;
        }
    }

    public void ignore(boolean ignore) {
        mIgnore = ignore;
        postInvalidate();
    }
}
