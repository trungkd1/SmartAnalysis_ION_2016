/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.co.fujixerox.sa.ion.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.interfaces.IBitmapDisplay;

/**
 * A view representing bitmap with XY coordinates axis
 * Created by TrungKD
 */
public class AxisImageView extends View implements IBitmapDisplay {
    private static final String TAG = "AxisImageView";
    private final float CIRCLE_RADIUS = 35;
    // Viewport extremes. See mCurrentViewport for a discussion of the viewport.
    private static final float AXIS_X_MIN = 0;//-1f;
    private static final float AXIS_X_MAX = 1f;
    private static final float AXIS_Y_MIN = 0;//-1f;
    private static final float AXIS_Y_MAX = 1f;
    /**
     * Cache analysis origin bitmap 8s
     */
    private Bitmap mBitmapCached;
    /**
     * Cache analysis crop bitmap for 1s or 5s
     */
    private Bitmap mCropBitmapCached;
    /**
     * The current viewport. This rectangle represents the currently visible chart domain
     * and range. The currently visible chart X values are from this rectangle's left to its right.
     * The currently visible chart Y values are from this rectangle's top to its bottom.
     * <p>
     * Note that this rectangle's top is actually the smaller Y value, and its bottom is the larger
     * Y value. Since the chart is drawn onscreen in such a way that chart Y values increase
     * towards the top of the screen (decreasing pixel Y positions), this rectangle's "top" is drawn
     * above this rectangle's "bottom" value.
     *
     * @see #mContentRect
     */
    private RectF mCurrentViewport = new RectF(AXIS_X_MIN, AXIS_Y_MIN, AXIS_X_MAX, AXIS_Y_MAX);

    /**
     * The current destination rectangle (in pixel coordinates) into which the chart data should
     * be drawn. Chart labels are drawn outside this area.
     *
     * @see #mCurrentViewport
     */
    private Rect mContentRect = new Rect();

    // Current attribute values and Paints.
    private float mLabelTextSize;
    private int mLabelSeparation;
    private int mLabelTextColor;
    private Paint mLabelTextPaint;
    private int mMaxLabelWidth;
    private int mLabelHeight;
    private float mAxisThickness;
    private int mAxisColor;
    private Paint mTextPaint;
    private Paint mAxisPaint;
    private Paint mDashedLinePaint;
    private PointF pointF1 = new PointF();
    private PointF pointF2 = new PointF();
    // Seek bar variables
    private float mCurrentSeekPositionX;
    private Paint mSeekBarPaint;
    private float mSeekStep;
    /**
     times refresh position in milliseconds
      */
    private int mSeekPeriod = 30;
    private int mSeekColor;
    private float mSeekStrokeWidth = 5;
    private Timer timer;
    private boolean mIsPause = false;
    private String mUnitY;
    private String mUnitX;

    /**
     Check is on or out circle
     */
    private boolean isOnCircle1 = false;
    private boolean isOnCircle2 = false;
    private boolean isDrawCircle = false;

    // Select area variables
    private float mSelectStrokeWidth = 2;
    private int mSelectAreaColor;

    // Buffers for storing current X and Y stops. See the computeAxisStops method for more details.
    private final AxisStops mXStopsBuffer = new AxisStops();
    private final AxisStops mYStopsBuffer = new AxisStops();

    // Buffers used during drawing. These are defined as fields to avoid allocation during
    // draw calls.
    private float[] mAxisXPositionsBuffer = new float[]{};
    private float[] mAxisYPositionsBuffer = new float[]{};

    private CharSequence[] mLabelX = null;
    private CharSequence[] mLabelY = null;
    /**
     * Analysis bitmap scale<br>
     * 8s : scale = 1<br>
     * 5s : scale = 1.375<br>
     * 1s : scale = 1.875<br>
     */
    private double mBitmapScale = 1;

    private OnSelectedAreaListener mOnSelectedArea;

    /**
     * Text show message in center of view
     * @param context
     */
    private String text;

    public AxisImageView(Context context) {
        this(context, null, 0);
    }

    public AxisImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AxisImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFocusable(true); // necessary for getting the touch events
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.AxisImageView, defStyle, defStyle);

        try {
            mLabelTextColor = a.getColor(
                    R.styleable.AxisImageView_labelTextColor, mLabelTextColor);
            mLabelTextSize = a.getDimension(
                    R.styleable.AxisImageView_labelTextSize, mLabelTextSize);
            mLabelSeparation = a.getDimensionPixelSize(
                    R.styleable.AxisImageView_labelSeparation, mLabelSeparation);


            mAxisThickness = a.getDimension(
                    R.styleable.AxisImageView_axisThickness, mAxisThickness);
            mAxisColor = a.getColor(
                    R.styleable.AxisImageView_axisColor, mAxisColor);

            mLabelX = a.getTextArray(R.styleable.AxisImageView_labelX);
            mLabelY = a.getTextArray(R.styleable.AxisImageView_labelY);
            mUnitY = a.getString(R.styleable.AxisImageView_unitY);
            mUnitX = a.getString(R.styleable.AxisImageView_unitX);
            mSeekPeriod = a.getInt(R.styleable.AxisImageView_seekBarPeriod, mSeekPeriod);
            mSeekColor = a.getColor(R.styleable.AxisImageView_seekBarColor, mSeekColor);
            mSeekStrokeWidth = a.getDimension(R.styleable.AxisImageView_seekBarStrokeWidth, mSeekStrokeWidth);
            mSelectAreaColor = a.getInt(R.styleable.AxisImageView_selectAreaColor, mSelectAreaColor);
            mSelectStrokeWidth = a.getDimension(R.styleable.AxisImageView_selectAreaStrokeWidth, mSelectStrokeWidth);
        } finally {
            a.recycle();
        }

        mCurrentSeekPositionX = mCurrentViewport.left;
        initPaints();
    }

    /**
     * (Re)initializes {@link Paint} objects based on current attribute values.
     */
    private void initPaints() {
        mLabelTextPaint = new Paint();
        mLabelTextPaint.setAntiAlias(true);
        mLabelTextPaint.setTextSize(mLabelTextSize);
        mLabelTextPaint.setColor(mLabelTextColor);
        mLabelHeight = (int) Math.abs(mLabelTextPaint.getFontMetrics().top);
        mMaxLabelWidth = (int) mLabelTextPaint.measureText("0000");

        mAxisPaint = new Paint();
        mAxisPaint.setStrokeWidth(mAxisThickness);
        mAxisPaint.setColor(mAxisColor);
        mAxisPaint.setStyle(Paint.Style.FILL);

        mDashedLinePaint = new Paint();
        mDashedLinePaint.setColor(mSelectAreaColor);
        mDashedLinePaint.setStyle(Paint.Style.STROKE);
        mDashedLinePaint.setStrokeWidth(mSelectStrokeWidth);
        mDashedLinePaint.setPathEffect(new DashPathEffect(new float[]{10, 20}, 0));

        mSeekBarPaint = new Paint();
        mSeekBarPaint.setColor(mSeekColor);
        mSeekBarPaint.setStyle(Paint.Style.STROKE);
        mSeekBarPaint.setStrokeWidth(mSeekStrokeWidth);

        mTextPaint = new Paint();
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mContentRect.set(
                getPaddingLeft() + mMaxLabelWidth + mLabelSeparation,
                getPaddingTop(),
                getWidth() - getPaddingRight(),
                getHeight() - getPaddingBottom() - mLabelHeight - mLabelSeparation);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minChartSize = getResources().getDimensionPixelSize(R.dimen.min_chart_size);
        setMeasuredDimension(
                Math.max(getSuggestedMinimumWidth(),
                        resolveSize(minChartSize + getPaddingLeft() + mMaxLabelWidth
                                        + mLabelSeparation + getPaddingRight(),
                                widthMeasureSpec)),
                Math.max(getSuggestedMinimumHeight(),
                        resolveSize(minChartSize + getPaddingTop() + mLabelHeight
                                        + mLabelSeparation + getPaddingBottom(),
                                heightMeasureSpec)));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Methods and objects related to drawing
    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onDraw(Canvas canvas) {
//        Log.v(TAG, "onDraw");
        super.onDraw(canvas);

        // Draws axes and text labels
        drawAxes(canvas);

        // Clips the next few drawing operations to the content area
        int clipRestoreCount = canvas.save();
        canvas.clipRect(mContentRect);

        // Removes clipping rectangle
        canvas.restoreToCount(clipRestoreCount);

        // Draws chart container
        canvas.drawRect(mContentRect, mAxisPaint);
        drawBitmap(canvas);

        drawResizedRectangle(canvas);

        drawSeekBar(canvas);

        drawText(canvas);
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mOnSelectedArea == null) {
            return true;
        }

        int X = (int) event.getX();
        int Y = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: // touch down so check if the finger is on
                double centerCircle1 =  Math.pow(event.getX() - getDrawX(pointF1.x),2) +  Math.pow((event.getY() - getDrawY(pointF1.y)),2);
                double centerCircle2 =  Math.pow(event.getX() - getDrawX(pointF2.x),2) +  Math.pow((event.getY() - getDrawY(pointF2.y)),2);
                float circles =  CIRCLE_RADIUS * CIRCLE_RADIUS;

                if( centerCircle1 <= circles){
                    isOnCircle1 = true;
                    isOnCircle2 = false;
                }else if( centerCircle2 <= circles){
                    isOnCircle1 = false;
                    isOnCircle2 = true;
                }else {
                    isOnCircle1 = false;
                    isOnCircle2 = false;
//                    pointF1.x = getPortX(X, 0, 1);
//                    pointF1.y = getPortY(Y, 0, 1);
//                    //reset pointF2
//                    pointF2.x = pointF1.x;
//                    pointF2.y = pointF1.y;
                }
                break;

            case MotionEvent.ACTION_MOVE: // touch drag with the ball

                if(isOnCircle1){
                    pointF1.x = getPortX(X, 0, 1);
                    pointF1.y = getPortY(Y, 0, 1);
                }else if(isOnCircle2){
                    // move the balls
                    pointF2.x = getPortX(X, 0, 1);
                    pointF2.y = getPortY(Y, 0, 1);
                }

                break;

            case MotionEvent.ACTION_UP:
                // touch drop - just do things here after dropping
                mOnSelectedArea.updateSelectedArea(pointF1, pointF2);
                break;
        }
        // redraw the canvas
        invalidate();
        return true;

    }

    /**
     * Draw text
     */
    private void drawText(Canvas canvas) {
        if (!TextUtils.isEmpty(text)) {
            int xPos = (canvas.getWidth() / 2);
            int yPos = (int) ((canvas.getHeight() / 2) - ((mTextPaint.descent() + mTextPaint.ascent()) / 2)) ;
            canvas.drawText(text, xPos, yPos, mTextPaint);
        }
    }


    /**
     * Set listener for update select area
     */
    public void setOnSelectedAreaListener(OnSelectedAreaListener listener) {
        this.mOnSelectedArea = listener;
    }

    /**
     * Set analysis bitmap to draw in screen
     * @param bitmap analysis bitmap
     */
    public void setBitmap(Bitmap bitmap) {
        mBitmapCached = null;
        mCropBitmapCached = null;
        mBitmapCached = bitmap;
        resetSelectedArea();
        invalidate();
    }

    public Bitmap getBitmap() {
        return mBitmapCached;
    }
    /**
     * Reset selected area
     */
    public void resetSelectedArea() {
        //reset selected area when bitmap have been changed
        pointF1.x = -1;
    }

    public void setSelectedAreaRect(PointF point1, PointF point2) {
        this.pointF1 = point1;
        this.pointF2 = point2;
        invalidate();
    }
    /**
     * Draw analysis bitmap into screen
     * @param canvas canvas for draw bitmap
     */
    private void drawBitmap(Canvas canvas) {
        if (mBitmapCached == null) return;
        int w = mBitmapCached.getWidth();
        int h = mBitmapCached.getHeight();
        if (mBitmapScale < 1) {
            if (mCropBitmapCached == null) { //1s or 5s
                mCropBitmapCached = Bitmap.createBitmap(mBitmapCached, 0, 0, (int) (w * mBitmapScale), h);
            }
            canvas.drawBitmap(mCropBitmapCached, null, mContentRect, null);
        } else {
            canvas.drawBitmap(mBitmapCached, null, mContentRect, null);
        }
    }


    /**
     * Draw select area from two coordinates
     * @param canvas canvas for draw area selected
     */
    private void drawResizedRectangle(Canvas canvas) {
        if (pointF1.x < 0) return; //rectangle coordinate is rested
        if (isDrawCircle) {
            //draw circle 1
            canvas.drawCircle(getDrawX(pointF1.x), getDrawY(pointF1.y), CIRCLE_RADIUS, mDashedLinePaint);
            //draw circle 2
            canvas.drawCircle(getDrawX(pointF2.x), getDrawY(pointF2.y), CIRCLE_RADIUS, mDashedLinePaint);
            //draw rectangle
        }
        if (pointF1.x < pointF2.x) {
            if (pointF1.y > pointF2.y) {
                canvas.drawRect(getDrawX(pointF1.x), getDrawY(pointF1.y),
                        getDrawX(pointF2.x), getDrawY(pointF2.y), mDashedLinePaint);
            } else {
                canvas.drawRect(getDrawX(pointF1.x), getDrawY(pointF2.y),
                        getDrawX(pointF2.x), getDrawY(pointF1.y), mDashedLinePaint);
            }
        } else {
            if (pointF1.y > pointF2.y) {
                canvas.drawRect(getDrawX(pointF2.x), getDrawY(pointF1.y),
                        getDrawX(pointF1.x), getDrawY(pointF2.y), mDashedLinePaint);
            } else {
                canvas.drawRect(getDrawX(pointF2.x), getDrawY(pointF2.y),
                        getDrawX(pointF1.x), getDrawY(pointF1.y), mDashedLinePaint);
            }
        }
    }

    /**
     * Draw seek bar
     */
    private void drawSeekBar(Canvas canvas) {
        float startX = getDrawX(mCurrentSeekPositionX);
        float startY = getDrawY(mCurrentViewport.top);
        float stopX = getDrawX(mCurrentSeekPositionX);
        float stopY =  getDrawY(mCurrentViewport.bottom);
        canvas.drawLine(startX, startY, stopX, stopY, mSeekBarPaint);
    }

    /**
     * Begin start seek bar belong to audio player
     * @param duration audio duration
     */
    public void startSeek(int duration, final MediaPlayer.OnCompletionListener callback) {
        if (mIsPause) {
            mIsPause = false; //resume
        } else {
            mSeekStep = mCurrentViewport.width() * mSeekPeriod / duration;
            timer = new Timer("seek");

            final TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    if (mIsPause) return;
                    mCurrentSeekPositionX += mSeekStep;
                    if (mCurrentSeekPositionX >= mCurrentViewport.right) {
                        mCurrentSeekPositionX = mCurrentViewport.left;
                        cancel();
                        timer.cancel();
                        if (callback != null) {
                            callback.onCompletion(null);
                        }
                    }
                    postInvalidate();
                }
            };
            timer.scheduleAtFixedRate(task, 0, mSeekPeriod);
        }
    }

    /**
     * Pause or Resume seek bar
     */
    public void pauseSeek() {
        mIsPause = true;
    }

    /**
     * Finish run seek bar
     */
    public void stopSeek() {
        mIsPause = false;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        mCurrentSeekPositionX = mCurrentViewport.left;
        invalidate();
    }

    /**
     * Draws the chart axes and labels onto the canvas.
     */
    private void drawAxes(Canvas canvas) {
        // Computes axis stops (in terms of numerical value and position on screen)
        int i;
        makeFixedAxisStops(mCurrentViewport.left, mCurrentViewport.right, mLabelX, mXStopsBuffer);
        makeFixedAxisStops(mCurrentViewport.top, mCurrentViewport.bottom, mLabelY, mYStopsBuffer);

        // Avoid unnecessary allocations during drawing. Re-use allocated
        // arrays and only reallocate if the number of stops grows.
        if (mAxisXPositionsBuffer.length < mXStopsBuffer.numStops) {
            mAxisXPositionsBuffer = new float[mXStopsBuffer.numStops];
        }
        if (mAxisYPositionsBuffer.length < mYStopsBuffer.numStops) {
            mAxisYPositionsBuffer = new float[mYStopsBuffer.numStops];
        }

        // Compute positions
        for (i = 0; i < mXStopsBuffer.numStops; i++) {
            mAxisXPositionsBuffer[i] = getDrawX(mXStopsBuffer.stops[i]);
        }
        for (i = 0; i < mYStopsBuffer.numStops; i++) {
            mAxisYPositionsBuffer[i] = getDrawY(mYStopsBuffer.stops[i]);
        }

        // Draws X labels
        mLabelTextPaint.setTextAlign(Paint.Align.CENTER);
        for (i = 0; i < mXStopsBuffer.numStops; i++) {
            // Do not use String.format in high-performance code such as onDraw code.
            canvas.drawText(
                   String.valueOf(mXStopsBuffer.labels[i]),
                    mAxisXPositionsBuffer[i],
                    mContentRect.bottom + mLabelHeight + mLabelSeparation,
                    mLabelTextPaint);
        }

        // Draws Y labels
        mLabelTextPaint.setTextAlign(Paint.Align.RIGHT);
        for (i = 0; i < mYStopsBuffer.numStops; i++) {
            // Do not use String.format in high-performance code such as onDraw code.
            canvas.drawText(
                    String.valueOf(mYStopsBuffer.labels[i]),
                    mContentRect.left - mLabelSeparation,
                    mAxisYPositionsBuffer[i] + mLabelHeight / 2,
                    mLabelTextPaint);
            canvas.drawLine(mContentRect.left- mLabelSeparation, mAxisYPositionsBuffer[i],
                    mContentRect.left, mAxisYPositionsBuffer[i], mAxisPaint);
        }
        // Draw Y unit (KHz)
        canvas.drawText(mUnitY, getDrawX(0), getDrawY(1)-mLabelSeparation, mLabelTextPaint);

        // Draw X unit (s)
        canvas.drawText(mUnitX, getDrawX(1)+mLabelSeparation*2,
                getDrawY(0)+mLabelSeparation+mLabelHeight, mLabelTextPaint);
    }

    /**
     * Computes the set of axis labels to show given start and stop boundaries and an ideal number
     * of stops between these boundaries.
     *
     * @param start The minimum extreme (e.g. the left edge) for the axis.
     * @param stop The maximum extreme (e.g. the right edge) for the axis.
     * @param steps The ideal number of stops to create. This should be based on available screen
     *              space; the more space there is, the more stops should be shown.
     * @param outStops The destination {@link AxisStops} object to populate.
     */
    private static void makeFixedAxisStops(float start, float stop, CharSequence[] steps, AxisStops outStops) {
        double range = stop - start;
        if (steps.length == 0 || range <= 0) {
            outStops.stops = new float[]{};
            outStops.numStops = 0;
            return;
        }
        double rawInterval = range / (steps.length-1);
        outStops.stops = new float[steps.length];
        outStops.labels = steps;
        outStops.numStops = steps.length;
        for (int i = 0; i < steps.length; i++) {
            outStops.stops[i] = (float) (i * rawInterval);
        }
    }

    /**
     * Change axis labelX
     */
    public void changeAxisLabelX(CharSequence[] labelX) {
        this.mLabelX = labelX;
        resetSelectedArea();
        invalidate();
    }

    /**
     * Computes the pixel offset for the given X chart value. This may be outside the view bounds.
     */
    private float getDrawX(float x) {
        return mContentRect.left
                + mContentRect.width()
                * (x - mCurrentViewport.left) / mCurrentViewport.width();
    }

    /**
     * Computes the pixel offset for the given Y chart value. This may be outside the view bounds.
     */
    private float getDrawY(float y) {
        return mContentRect.bottom
                - mContentRect.height()
                * (y - mCurrentViewport.top) / mCurrentViewport.height();
    }

    /**
     * Computes the port view offset for the given draw X chart value. This may be outside the view bounds.
     */
    private float getPortX(float drawX) {
        return mCurrentViewport.left + ((drawX - mContentRect.left)*mCurrentViewport.width())/mContentRect.width();
    }

    /**
     * Computes the port view offset for the given Y chart value. This may be outside the view bounds.
     */
    private float getPortY(float drawY) {
        return mCurrentViewport.top - ((drawY - mContentRect.bottom)*mCurrentViewport.height())/mContentRect.height();
    }

    /**
     * Computes the port view offset for the given draw X chart value. This will be constraint by min/max bounds.
     */
    private float getPortX(float drawX, float min, float max) {
        float result = getPortX(drawX);
        if (result > max) {
            result = max;
        } else if (result < min) {
            result = min;
        }
        return result;
    }

    /**
     * Computes the port view offset for the given draw Y chart value. This will be constraint by min/max bounds.
     */
    private float getPortY(float drawY, float min, float max) {
        float result = getPortY(drawY);
        if (result > max) {
            result = max;
        } else if (result < min) {
            result = min;
        }
        return result;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Methods and objects related to gesture handling
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Finds the chart point (i.e. within the chart's domain and range) represented by the
     * given pixel coordinates, if that pixel is within the chart region described by
     * {@link #mContentRect}. If the point is found, the "dest" argument is set to the point and
     * this function returns true. Otherwise, this function returns false and "dest" is unchanged.
     */
    private boolean hitTest(float x, float y, PointF dest) {
        if (!mContentRect.contains((int) x, (int) y)) {
            return false;
        }

        dest.set(
                mCurrentViewport.left
                        + mCurrentViewport.width()
                        * (x - mContentRect.left) / mContentRect.width(),
                mCurrentViewport.top
                        + mCurrentViewport.height()
                        * (y - mContentRect.bottom) / -mContentRect.height());
        return true;
     }

    /**
     * Ensures that current viewport is inside the viewport extremes defined by {@link #AXIS_X_MIN},
     * {@link #AXIS_X_MAX}, {@link #AXIS_Y_MIN} and {@link #AXIS_Y_MAX}.
     */
    private void constrainViewport() {
        mCurrentViewport.left = Math.max(AXIS_X_MIN, mCurrentViewport.left);
        mCurrentViewport.top = Math.max(AXIS_Y_MIN, mCurrentViewport.top);
        mCurrentViewport.bottom = Math.max(Math.nextUp(mCurrentViewport.top),
                Math.min(AXIS_Y_MAX, mCurrentViewport.bottom));
        mCurrentViewport.right = Math.max(Math.nextUp(mCurrentViewport.left),
                Math.min(AXIS_X_MAX, mCurrentViewport.right));
    }

    /**
     * Sets the current viewport (defined by {@link #mCurrentViewport}) to the given
     * X and Y positions. Note that the Y value represents the topmost pixel position, and thus
     * the bottom of the {@link #mCurrentViewport} rectangle. For more details on why top and
     * bottom are flipped, see {@link #mCurrentViewport}.
     */
    private void setViewportBottomLeft(float x, float y) {
        /**
         * Constrains within the scroll range. The scroll range is simply the viewport extremes
         * (AXIS_X_MAX, etc.) minus the viewport size. For example, if the extrema were 0 and 10,
         * and the viewport size was 2, the scroll range would be 0 to 8.
         */

        float curWidth = mCurrentViewport.width();
        float curHeight = mCurrentViewport.height();
        x = Math.max(AXIS_X_MIN, Math.min(x, AXIS_X_MAX - curWidth));
        y = Math.max(AXIS_Y_MIN + curHeight, Math.min(y, AXIS_Y_MAX));

        mCurrentViewport.set(x, y - curHeight, x + curWidth, y);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Methods for programmatically changing the viewport
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the current viewport (visible extremes for the chart domain and range.)
     */
    public RectF getCurrentViewport() {
        return new RectF(mCurrentViewport);
    }

    /**
     * Sets the chart's current viewport.
     *
     * @see #getCurrentViewport()
     */
    public void setCurrentViewport(RectF viewport) {
        mCurrentViewport = viewport;
        constrainViewport();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Methods related to custom attributes
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public float getLabelTextSize() {
        return mLabelTextSize;
    }

    public void setLabelTextSize(float labelTextSize) {
        mLabelTextSize = labelTextSize;
        initPaints();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public int getLabelTextColor() {
        return mLabelTextColor;
    }

    public void setLabelTextColor(int labelTextColor) {
        mLabelTextColor = labelTextColor;
        initPaints();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public float getAxisThickness() {
        return mAxisThickness;
    }

    public void setAxisThickness(float axisThickness) {
        mAxisThickness = axisThickness;
        initPaints();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public int getAxisColor() {
        return mAxisColor;
    }

    public void setAxisColor(int axisColor) {
        mAxisColor = axisColor;
        initPaints();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Methods and classes related to view state persistence.
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.viewport = mCurrentViewport;
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        mCurrentViewport = ss.viewport;
    }

    public void setBitmapScale(double bitmapScale) {
        if (mBitmapScale != bitmapScale && bitmapScale != 1) {
            mCropBitmapCached = null;
        }
        this.mBitmapScale = bitmapScale;
        invalidate();
    }

    @Override
    public void setBitmapDisplay(Bitmap bitmap) {
        setBitmap(bitmap);
    }

    /**
     * Persistent state that is saved by FxGraphView.
     */
    public static class SavedState extends BaseSavedState {
        private RectF viewport;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeFloat(viewport.left);
            out.writeFloat(viewport.top);
            out.writeFloat(viewport.right);
            out.writeFloat(viewport.bottom);
        }

        @Override
        public String toString() {
            return "FxGraphView.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " viewport=" + viewport.toString() + "}";
        }

        public static final Creator<SavedState> CREATOR
                = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        });

        SavedState(Parcel in) {
            super(in);
            viewport = new RectF(in.readFloat(), in.readFloat(), in.readFloat(), in.readFloat());
        }
    }

    /**
     * A simple class representing axis label values.
     *
     * @see #makeFixedAxisStops(float, float, CharSequence[], AxisStops)
     */
    private static class AxisStops {
        float[] stops = new float[]{};
        CharSequence[] labels = new CharSequence[]{};
        int numStops;
    }

    public interface OnSelectedAreaListener {
        void updateSelectedArea(PointF point1, PointF point2);
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setDrawCircle(boolean drawCircle) {
        isDrawCircle = drawCircle;
    }
}
