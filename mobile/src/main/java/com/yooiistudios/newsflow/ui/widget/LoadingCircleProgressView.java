package com.yooiistudios.newsflow.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.yooiistudios.newsflow.R;

/**
 * Created by Wooseong Kim in NewsLoadingAnimation from Yooii Studios Co., LTD. on 15. 3. 23.
 *
 * LoadingCircleProgressView
 *  로딩이 끝난 후 후속 Arc 애니메이션 뷰
 */
public class LoadingCircleProgressView extends View {
    public interface CircleAnimationListener {
        public void onCircleAnimationEnd();
    }

    private static final int PROGRESS_START_DEGREE = 270;

    private static final float SWEEP_OFFSET = 21;

    private float mCircleSize;
    private RectF mCircleRect;
    private Paint mCirclePaint;

    private Bitmap mBitmap;
    private Canvas mClearCanvas;
    private Paint mClearPaint;

    private float mSweep = 0;
    private int mBackgroundColor;
    private boolean mNeedToBeAnimated = false;
    private CircleAnimationListener mListener;

    public LoadingCircleProgressView(Context context) {
        super(context);
        init();
    }

    public LoadingCircleProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadingCircleProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // xml 상에서 잘 보이게 따로 값 수정
        if (isInEditMode()) {
            mSweep = 359;
            mNeedToBeAnimated = true;
        }

        float strokeWidth = getResources().getDimension(R.dimen.loading_circle_stroke_width);

        initCirclePaint(strokeWidth);
        initCircleStuff();

        initClearPaint(strokeWidth);
        initClearCanvas();

        mBackgroundColor = getResources().getColor(R.color.material_light_blue_A700);
    }

    private void initClearCanvas() {
        mBitmap = Bitmap.createBitmap((int) mCircleSize, (int) mCircleSize, Bitmap.Config.ARGB_8888);
        mBitmap.eraseColor(Color.TRANSPARENT);
        mClearCanvas = new Canvas(mBitmap);
    }

    private void initCircleStuff() {
        mCircleSize = getResources().getDimension(R.dimen.loading_circle_view_size);
        mCircleRect = new RectF(0, 0, mCircleSize, mCircleSize);
    }

    private void initClearPaint(float strokeWidth) {
        mClearPaint = new Paint();
        mClearPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mClearPaint.setStrokeWidth(strokeWidth);
        mClearPaint.setColor(Color.TRANSPARENT);
        mClearPaint.setAntiAlias(true);
        mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    private void initCirclePaint(float strokeWidth) {
        mCirclePaint = new Paint();
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(strokeWidth);
        mCirclePaint.setColor(Color.WHITE);
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void startCircleAnimation(CircleAnimationListener listener) {
        mListener = listener;
        mNeedToBeAnimated = true;
        invalidate();
    }

    public void invalidateWidthCircleSize(float circleSize) {
        mCircleSize = circleSize;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        calculateCircleRect();

        mClearCanvas.drawColor(mBackgroundColor);
        mClearCanvas.drawOval(mCircleRect, mClearPaint);

        canvas.drawBitmap(mBitmap, 0, 0, null);
        canvas.drawArc(mCircleRect, PROGRESS_START_DEGREE, -mSweep, false, mCirclePaint);

        if (mNeedToBeAnimated) {
            increaseSweep();
            invalidate();
        }
    }

    private void calculateCircleRect() {
        float offset = getWidth() - mCircleSize;
        if (mCircleSize >= getWidth()) {
            mCircleRect.set(offset, offset, mCircleSize, mCircleSize);
        } else {
            float leftTop = offset / 2;
            mCircleRect.set(leftTop, leftTop, leftTop + mCircleSize, leftTop + mCircleSize);
        }
    }

    private void increaseSweep() {
        // 이를 sweep 이 0도 ~ 360도로 변환하게 내부적으로 변경하자
        mSweep += SWEEP_OFFSET;
        if (mSweep > 360) {
            mSweep = 360;
            mNeedToBeAnimated = false;
            if (mListener != null) {
                mListener.onCircleAnimationEnd();
            }
        }
    }
}
