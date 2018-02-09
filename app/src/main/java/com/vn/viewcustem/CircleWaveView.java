package com.vn.viewcustem;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.vn.code.R;

public class CircleWaveView extends View implements Runnable {

    private final float ANGLE_OFFSET = -90;
    private static final int PERIOD = 25;

    private String mTimeLeftText = "";
    private String mTimeLeftValue = "";

    private int mTextColor = 0xCCFFFFFF;
    private float mOutStrokeWidth = 10;
    private Point mCenterPoint = new Point(0, 0);
    int mCurrentHeight = 0;
    int mRadius = 0;
    int mOutRadius = 0;
    boolean mStart = false;
    float mTextSise = 50;
    int mTranX = 0;
    private Paint mCirclePaint;
    private Paint mOutCirclePaint;
    private Paint mWaterPaint;
    private Paint mTextPaint;

    private Paint mGlowCirclePaint;
    private RectF mArcRect = new RectF();
    private Paint mPointPaint;
    private Paint mGlowPointPaint;

    private boolean drawNeon = true;
    private float mIndicatorIconX, mIndicatorIconY;
    private float mProgressAngle = -1;
    private float mProgressTarget = -1;
    private int mAngleSpeed = 3;

    private boolean isDrawing = false;
    private int mCircleColor = Color.parseColor("#D2F557");
    private int mOutStrokeColor = Color.parseColor("#B8D86A");
    private int mGLowColor = Color.parseColor("#B8D86A");
    private int mWaterColor = Color.parseColor("#04DD98");
    private int mWaterTaget = 0;
    private int flowNum = 0;
    private int mWaterSpeed = 10;
    private int mUpSpeed = 2;
    private int max = 100;
    private int progress = 0;
    private float amplitude = 1f;
    private float increase = 6f;

    private CircleWaveViewListener listener;

    public CircleWaveView(Context context) {
        super(context);
        init(null, 0);
    }

    public CircleWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CircleWaveView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        loadAttrs(attrs, defStyle);

        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setDither(true);
        mCirclePaint.setColor(mOutStrokeColor);
        mCirclePaint.setStrokeWidth(mOutStrokeWidth);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeJoin(Paint.Join.ROUND);
        mCirclePaint.setStrokeCap(Paint.Cap.ROUND);
        mCirclePaint.setPathEffect(new CornerPathEffect(10));

        mPointPaint = new Paint(mCirclePaint);
        mPointPaint.setStyle(Paint.Style.FILL);

        mGlowCirclePaint = new Paint();
        mGlowCirclePaint.set(mCirclePaint);
        mGlowCirclePaint.setColor(mGLowColor);
        mGlowCirclePaint.setColor(Color.argb(255, 255, 255, 255));
        mGlowCirclePaint.setStrokeWidth(mOutStrokeWidth * 1f);
        mGlowCirclePaint.setMaskFilter(new BlurMaskFilter(mOutStrokeWidth * 1.5f, BlurMaskFilter.Blur.NORMAL));
        mGlowCirclePaint.setPathEffect(new CornerPathEffect(10));

        mGlowPointPaint = new Paint(mGlowCirclePaint);
        mGlowPointPaint.setStyle(Paint.Style.FILL);

        mOutCirclePaint = new Paint();
        mOutCirclePaint.setColor(mOutStrokeColor);
        mOutCirclePaint.setStyle(Paint.Style.FILL);
        mOutCirclePaint.setAntiAlias(true);

        mWaterPaint = new Paint();
        mWaterPaint.setStrokeWidth(1.0F);
        mWaterPaint.setColor(mWaterColor);
        mWaterPaint.setStyle(Paint.Style.FILL);
        mWaterPaint.setAntiAlias(true);

        mTextPaint = new Paint();
        mTextPaint.setStrokeWidth(1.0F);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSise);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setAntiAlias(true);
    }

    private void loadAttrs(AttributeSet attrs, int defStyle) {
        TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.CircleWaveView, defStyle, 0);
        mTextColor = a.getColor(
                R.styleable.CircleWaveView_textColor,
                mTextColor);
        mTextSise = a.getDimension(R.styleable.CircleWaveView_textSize, mTextSise);
        mCircleColor = a.getColor(R.styleable.CircleWaveView_backgroudColor, mCircleColor);
        mOutStrokeColor = a.getColor(R.styleable.CircleWaveView_strokeColor, mOutStrokeColor);
        mGLowColor = a.getColor(R.styleable.CircleWaveView_glowColor, mGLowColor);
        mWaterColor = a.getColor(R.styleable.CircleWaveView_waterColor, mWaterColor);
        progress = a.getInt(R.styleable.CircleWaveView_progressWave, progress);
        if (progress < 0) {
            throw new RuntimeException("progress can not less than 0");
        }
        max = a.getInt(R.styleable.CircleWaveView_max, max);
        mOutStrokeWidth = a.getDimension(R.styleable.CircleWaveView_strokeSize, mOutStrokeWidth);
        amplitude = a.getFloat(R.styleable.CircleWaveView_amplitude, amplitude);
        increase = a.getFloat(R.styleable.CircleWaveView_increase, increase);
        mWaterSpeed = a.getInt(R.styleable.CircleWaveView_waterSpeed, mWaterSpeed);
        mUpSpeed = a.getInt(R.styleable.CircleWaveView_upSpeed, mUpSpeed);
        drawNeon = a.getBoolean(R.styleable.CircleWaveView_drawNeon, drawNeon);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        int minLength = Math.min(width, height);
        mOutRadius = minLength / 2;
        mRadius = (int) (0.5 * (minLength - mOutStrokeWidth));
        mCenterPoint = new Point(minLength / 2, minLength / 2);
        if (progress != 0) {
            setProgress(progress);
            calculateIndicatorPosition();
        }

        mArcRect.set(mCenterPoint.x - (mOutRadius - mOutStrokeWidth * 2f),
                mCenterPoint.y - (mOutRadius - mOutStrokeWidth * 2f),
                mCenterPoint.x + mOutRadius - mOutStrokeWidth * 2f,
                mCenterPoint.y + mOutRadius - mOutStrokeWidth * 2f);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onAttachedToWindow() {
        isDrawing = true;
        new Thread(this).start();
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        isDrawing = false;
        super.onDetachedFromWindow();
    }

    @Override
    public void run() {
        while (isDrawing) {
            long startTime = System.currentTimeMillis();
            if (mWaterTaget > mCurrentHeight) {
                mCurrentHeight = mCurrentHeight + mUpSpeed;
                if (mWaterTaget <= mCurrentHeight) {
                    mCurrentHeight = mWaterTaget;
                }
            }
            if (drawNeon && mProgressTarget > mProgressAngle) {
                mProgressAngle += mAngleSpeed;
                if (mProgressAngle >= mProgressTarget) {
                    mProgressAngle = mProgressTarget;
                    if (listener != null)
                        listener.OnNeonReachedTarget(mProgressTarget);
                }
                calculateIndicatorPosition();
            }
            if (mStart) {
                if (mTranX > mRadius) {
                    mTranX = 0;
                }
                mTranX -= mWaterSpeed;
            }
            postInvalidate();
            long time = System.currentTimeMillis() - startTime;
            if (time < PERIOD) {
                try {
                    Thread.sleep(PERIOD - time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void calculateIndicatorPosition() {
        int angle = (int) (mProgressAngle + ANGLE_OFFSET);
        mIndicatorIconX = (float) ((mOutRadius - mOutStrokeWidth * 2f) * Math.cos(Math.toRadians(angle))) + mCenterPoint.x;
        mIndicatorIconY = (float) ((mOutRadius - mOutStrokeWidth * 2f) * Math.sin(Math.toRadians(angle))) + mCenterPoint.y;
    }

    public void updateIndicatorIconPosition(float degree) {
        mProgressTarget = degree;
    }

    public void updateIndicatorIconPosition(float degree, CircleWaveViewListener listener) {
        this.mProgressTarget = degree;
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawCanvas(canvas);
    }

    private void drawCanvas(Canvas canvas) {
        if (!isDrawing)
            return;

        if (mStart) {
            int mH = mCenterPoint.y + mRadius - mCurrentHeight;
            int length = 2 * mOutRadius;
            try {
                Path path = new Path();
                path.moveTo(0, mH);
                for (int x = 0; x < length; x++) {
                    int y = (int) (Math.sin(Math.toRadians(x + mTranX) / amplitude) * mRadius / increase);
                    path.lineTo(x, mH + y);
                }
                path.lineTo(length, mH);
                path.lineTo(length, mCenterPoint.y + mRadius);
                path.lineTo(0, mCenterPoint.y + mRadius);
                path.lineTo(0, mH);
                canvas.save();

                Path pc = new Path();
                pc.addCircle(mCenterPoint.x, mCenterPoint.y, mRadius - mOutStrokeWidth, Path.Direction.CCW);
                canvas.clipPath(pc, Region.Op.INTERSECT);
                canvas.drawPath(path, mWaterPaint);
            } catch (Exception e) {
                Log.e("Path", e.getMessage());
            }
            mTextPaint.setTextSize(mTextSise);
            if (flowNum >= 50)
                canvas.drawText(flowNum + "%", mCenterPoint.x, mCenterPoint.y - (getWidth() / 2) + mTextSise * 2, mTextPaint);
            else if (flowNum >= 30)
                canvas.drawText(flowNum + "%", mCenterPoint.x, mCenterPoint.y + mTextSise / 2, mTextPaint);
            else
                canvas.drawText(flowNum + "%", mCenterPoint.x, mCenterPoint.y + getWidth() / 2 - mTextSise, mTextPaint);
//            if (mTimeLeftText.equals("")) {
//                mTextPaint.setTextSize(mTextSise);
//                canvas.drawText(flowNum + "%", mCenterPoint.x, mCenterPoint.y + mTextSise / 2, mTextPaint);
//            } else {
//                mTextPaint.setTextSize(mTextSise);
//                canvas.drawText(flowNum + "%", mCenterPoint.x, mCenterPoint.y+mTextSise, mTextPaint);
//                mTextPaint.setTextSize(mTextSise * .35f);
            // canvas.drawText(mTimeLeftText, mCenterPoint.x, mCenterPoint.y + mTextSise * .65f, mTextPaint);
            // canvas.drawText(mTimeLeftValue, mCenterPoint.x, mCenterPoint.y + mTextSise * 1.05f, mTextPaint);
//            }

            canvas.restore();
        }

        if (drawNeon && mProgressAngle >= 0)
            canvas.drawArc(mArcRect, ANGLE_OFFSET, mProgressAngle, false, mGlowCirclePaint);
        canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mOutRadius - mOutStrokeWidth * 2f, mCirclePaint);

        if (drawNeon && mProgressAngle >= 0) {
            canvas.drawCircle(mIndicatorIconX, mIndicatorIconY, mOutStrokeWidth * 1.15f, mGlowPointPaint);
            canvas.drawCircle(mIndicatorIconX, mIndicatorIconY, mOutStrokeWidth, mPointPaint);
        }
    }

    public void setProgress(int progress) {
        if (progress < 0) {
            throw new RuntimeException("progress can not less than 0");
        }
        this.progress = progress;
        if (this.progress > max) {
            this.progress = max;
        }
        flowNum = this.progress * 100 / max;
        mStart = true;
        mWaterTaget = 2 * mRadius * progress / max;
    }


    public void setCircleColor(int mCircleColor) {
        this.mCircleColor = mCircleColor;
    }


    public void setOutStrokeColor(int mOutStrokeColor) {
        this.mOutStrokeColor = mOutStrokeColor;
    }

    public void setWaterColor(int mWaterColor) {
        this.mWaterColor = mWaterColor;
        mWaterPaint.setColor(mWaterColor);
    }

    public int getWaterSpeed() {
        return mWaterSpeed;
    }

    public void setWaterSpeed(int mWaterSpeed) {
        if (mWaterSpeed < 0) {
            throw new RuntimeException("WaterSpeed can not less than 0");
        }
        this.mWaterSpeed = mWaterSpeed;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        if (max < 0) {
            throw new RuntimeException("max can not less than 0");
        }
        this.max = max;
    }

    public int getProgress() {
        return progress;
    }

    public float getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(float amplitude) {
        this.amplitude = amplitude;
    }

    public float getIncrease() {
        return increase;
    }

    public void setIncrease(float increase) {
        this.increase = increase;
    }

    public void setDrawNeon(boolean drawNeon) {
        this.drawNeon = drawNeon;
    }

    public void setProgressAngle(float angle) {
        this.mProgressAngle = angle;
    }

    public void setTextColor(int textColor) {
        this.mTextColor = textColor;
    }

    public void setOutStrokeWidth(float mOutStrokeWidth) {
        this.mOutStrokeWidth = mOutStrokeWidth;
    }

    public void setTextSise(float s) {
        mTextSise = s;
        mTextPaint.setTextSize(s);
    }

    public void setWaveSpeed(int speed) {
        mWaterSpeed = speed;
    }

    public void setUpSpeed(int speed) {
        mUpSpeed = speed;
    }

    public void setColor(int waveColor, int circleColor, int outCircleColor) {
        mWaterColor = waveColor;
        mCircleColor = circleColor;
        mOutStrokeColor = outCircleColor;
        mWaterPaint.setColor(mWaterColor);
        mCirclePaint.setColor(mCircleColor);
        mOutCirclePaint.setColor(mOutStrokeColor);
    }

    public void setTimeLeft(String timeLeftText, String timeLeftValue) {
        mTimeLeftText = timeLeftText;
        mTimeLeftValue = timeLeftValue;
        invalidate();
    }
}