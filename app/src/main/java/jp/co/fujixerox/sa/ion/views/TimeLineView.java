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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import jp.co.fujixerox.sa.ion.R;

/**
 * A view representing timeline milestones
 * @author FPT
 */
public class TimeLineView extends View {
    private static final String TAG = "TimeLineView";
    private final float CIRCLE_RADIUS = 15;
    // Viewport extremes. See mCurrentViewport for a discussion of the viewport.
    private static final float AXIS_X_MIN = 0;//-1f;
    private static final float AXIS_X_MAX = 1f;
    private static final float AXIS_Y_MIN = 0;//-1f;
    private static final float AXIS_Y_MAX = 1f;

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
    private float mLineThickness = 2;
    private int mMilestoneActiveColor;
    private int mMilestoneInactiveColor;
    private int mCurrentMilestone;
    private Paint mMilestoneInactivePaint;
    private Paint mMilestoneActivePaint;
    // Buffers for storing current X and Y stops. See the computeAxisStops method for more details.
    private final AxisStops mXStopsBuffer = new AxisStops();

    // Buffers used during drawing. These are defined as fields to avoid allocation during
    // draw calls.
    private float[] mAxisXPositionsBuffer = new float[]{};

    private CharSequence[] mMilestonesLabels = null;


    public TimeLineView(Context context) {
        this(context, null, 0);
    }

    public TimeLineView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeLineView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFocusable(true); // necessary for getting the touch events
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.TimeLineView, defStyle, defStyle);

        try {
            mMilestoneActiveColor = a.getColor(
                    R.styleable.TimeLineView_milestonesActiveColor, mMilestoneActiveColor);
            mMilestoneInactiveColor = a.getColor(
                    R.styleable.TimeLineView_milestonesInactiveColor, mMilestoneInactiveColor);
            mLineThickness = a.getDimension(R.styleable.TimeLineView_lineThickness, mLineThickness);
            mMilestonesLabels = a.getTextArray(R.styleable.TimeLineView_milestonesLabels);
            if (mMilestonesLabels == null) {
                mMilestonesLabels = getResources().getStringArray(R.array.milestones);
            }
            mCurrentMilestone = a.getInteger(R.styleable.TimeLineView_currentMilestone, mCurrentMilestone);
            mLabelTextColor = a.getColor(
                    R.styleable.TimeLineView_milestonesTextColor, mLabelTextColor);
            mLabelTextSize = a.getDimension(
                    R.styleable.TimeLineView_milestonesTextSize, mLabelTextSize);
            mLabelSeparation = a.getDimensionPixelSize(
                    R.styleable.TimeLineView_milestonesTextSeparation, mLabelSeparation);
        } finally {
            a.recycle();
        }
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

        mMilestoneInactivePaint = new Paint();
        mMilestoneInactivePaint.setStrokeWidth(mLineThickness);
        mMilestoneInactivePaint.setColor(mMilestoneInactiveColor);
        mMilestoneInactivePaint.setStyle(Paint.Style.FILL);

        mMilestoneActivePaint = new Paint();
        mMilestoneActivePaint.setStrokeWidth(mLineThickness);
        mMilestoneActivePaint.setColor(mMilestoneActiveColor);
        mMilestoneActivePaint.setStyle(Paint.Style.FILL);
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
        int minChartSize = getResources().getDimensionPixelSize(R.dimen.min_timeline_size);
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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Methods and objects related to drawing
    //
    @Override
    protected void onDraw(Canvas canvas) {
        Log.v(TAG, "onDraw");
        super.onDraw(canvas);
        drawMilestones(canvas);
        drawMilestonesLine(canvas);
        drawMilestonesCircle(canvas);
    }

    /**
     * Draws the chart milestones labels onto the canvas.
     */
    private void drawMilestones(Canvas canvas) {
        // Computes axis stops (in terms of numerical value and position on screen)
        int i;
        makeFixedAxisStops(mCurrentViewport.left, mCurrentViewport.right, mMilestonesLabels, mXStopsBuffer);
        // Avoid unnecessary allocations during drawing. Re-use allocated
        // arrays and only reallocate if the number of stops grows.
        if (mAxisXPositionsBuffer.length < mXStopsBuffer.numStops) {
            mAxisXPositionsBuffer = new float[mXStopsBuffer.numStops];
        }
        float y = getDrawY(0.5f);
        // Compute positions
        for (i = 0; i < mXStopsBuffer.numStops; i++) {
            mAxisXPositionsBuffer[i] = getDrawX(mXStopsBuffer.stops[i]);
        }
        // Draws X labels
        mLabelTextPaint.setTextAlign(Paint.Align.CENTER);
        for (i = 0; i < mXStopsBuffer.numStops; i++) {
            // Do not use String.format in high-performance code such as onDraw code.
            canvas.drawText(
                    String.valueOf(mXStopsBuffer.labels[i]),
                    mAxisXPositionsBuffer[i],
                    y,
                    mLabelTextPaint);
        }

    }

    /**
     * Draw milestones circle
     * @param canvas Canvas
     */
    private void drawMilestonesCircle(Canvas canvas) {
        float y = getDrawY(0.05f);
        for (int i = 0; i < mXStopsBuffer.numStops; i++) {
            if (i <= mCurrentMilestone-1) {
                canvas.drawCircle(mAxisXPositionsBuffer[i], y, CIRCLE_RADIUS, mMilestoneActivePaint);
            } else {
                canvas.drawCircle(mAxisXPositionsBuffer[i], y, CIRCLE_RADIUS, mMilestoneInactivePaint);
            }
        }
    }

    /**
     * Draw line connect milestone circle
     * @param canvas Canvas
     */
    private void drawMilestonesLine(Canvas canvas) {
        float y = getDrawY(0.05f);
        for (int i = 0; i < mXStopsBuffer.numStops-1; i++) {
            if (i < mCurrentMilestone-1) {
                canvas.drawLine(mAxisXPositionsBuffer[i], y, mAxisXPositionsBuffer[i + 1], y, mMilestoneActivePaint);
            } else {
                canvas.drawLine(mAxisXPositionsBuffer[i], y, mAxisXPositionsBuffer[i + 1], y, mMilestoneInactivePaint);
            }
        }
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
        double interval = range/steps.length;
        double haftInterval = interval/3;
        outStops.stops = new float[steps.length];
        outStops.labels = steps;
        outStops.numStops = steps.length;
        for (int i = 0; i < steps.length; i++) {
            outStops.stops[i] = (float) (haftInterval + interval*i);
        }
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

    private float getPortX(float drawX) {
        return mCurrentViewport.left + ((drawX - mContentRect.left)*mCurrentViewport.width())/mContentRect.width();
    }

    private float getPortY(float drawY) {
        return mCurrentViewport.top - ((drawY - mContentRect.bottom)*mCurrentViewport.height())/mContentRect.height();
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
        return mLineThickness;
    }

    public void setAxisThickness(float axisThickness) {
        mLineThickness = axisThickness;
        initPaints();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public int getAxisColor() {
        return mMilestoneActiveColor;
    }

    public void setAxisColor(int axisColor) {
        mMilestoneActiveColor = axisColor;
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
}
