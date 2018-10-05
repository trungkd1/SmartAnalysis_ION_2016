package jp.co.fujixerox.sa.ion.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.media.MediaPlayer;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.utils.Utility;

/**
 * Created by TrungKD
 *         周波数軸と周期軸表示するクラス
 */
public class AxisViewController {
    private static final int FIVE_SECS = 5;
    private static final int EIGHT_SECS = 8;
    private static final int ONE_SEC = 1;
    private static final String TAG = "AxisViewController";
    private AxisImageView mFxGraphView;
    private Context mContext = null;
    private TextView mTvFileName = null;
    private int mOriginAxisTime = -1;
    private Bitmap DEFAULT_BMP = Bitmap.createBitmap(Utility.OPTIONS.WIDTH,
            Utility.OPTIONS.HEIGHT, Config.ARGB_8888);
    private Bitmap mBitmap = DEFAULT_BMP;

    public AxisViewController(Context context, AxisImageView fxGraphView, TextView tvFileName) {
        this.mContext = context;
        this.mFxGraphView = fxGraphView;
        this.mTvFileName = tvFileName;
        resetAnalysisImage();
    }

    /*
     * Reset imageview for a new analysis image
     */
    public void resetAnalysisImage() {
        // Default bimtmap
        setBitmap(DEFAULT_BMP);
        mOriginAxisTime = -1;
    }

    /**
     * Set analysed bitmap
     *
     * @param bitmap Bitmap
     */
    public void setBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        this.mBitmap = bitmap;
        mFxGraphView.setBitmap(bitmap);
    }

    /**
     * Set axis horizontal (time) values
     *
     * @param analysisTime
     */
    public void setAxisHorizontalValues(int analysisTime) {
        double bitmapScale = 1.0;
        if (analysisTime == Utility.ANALYSIS_TIME_VALUES[0]) { //1 sec
            bitmapScale = 0.125;
        } else if (analysisTime == Utility.ANALYSIS_TIME_VALUES[1]) { //5 secs
            bitmapScale = 0.625;
        }
        mFxGraphView.setBitmapScale(bitmapScale);
        mOriginAxisTime = analysisTime;
        // draw axis label for chart
        CharSequence[] axisTimeValues = null;
        Resources resources = mContext.getResources();
        switch (analysisTime) {
            case EIGHT_SECS: // 8secs CASE
                axisTimeValues = resources.getTextArray(R.array.axis_horizontal8s);
                break;
            case FIVE_SECS: // 5secs CASE
                axisTimeValues = resources.getTextArray(R.array.axis_horizontal5s);
                break;
            case ONE_SEC:
                axisTimeValues = resources.getTextArray(R.array.axis_horizontal1s);
                break;
        }
        // check if crop bitmap and scale greater than 0 then set new width to
        // image
        //todo resize bitmap and axis
        mFxGraphView.changeAxisLabelX(axisTimeValues);

    }

    public int getScale(int time) {
        if (mOriginAxisTime == 0) {
            return ((Utility.OPTIONS.WIDTH) / time);
        } else if (mOriginAxisTime == 1) {
            return ((Utility.OPTIONS.WIDTH * 5) / time);
        } else if (mOriginAxisTime == 2) {
            return ((Utility.OPTIONS.WIDTH * 8) / time);
        } else {
            return -1;
        }
    }

    /**
     * Set file name
     *
     * @param fileName
     */
    public void setFileName(String fileName) {
        if (fileName != null) {
            mTvFileName.setText(fileName);
        } else {
            mTvFileName.setText("");
            Log.e(TAG, "setFileName is null");
        }
    }

    /**
     * Convert pixel to dp unit
     *
     * @param px
     * @return
     */
    private int convertPixelToDp(int px) {
        int result = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, px, mContext.getResources()
                        .getDisplayMetrics());
        Log.d(TAG, "convertPixelToDp:from px:" + px);
        Log.d(TAG, "convertPixelToDp:to dp:" + result);
        return result;
    }

    public void startSeekbar(int duration, MediaPlayer.OnCompletionListener callback) {
        mFxGraphView.startSeek(duration, callback);
    }

    public void pauseSeekbar() {
        mFxGraphView.pauseSeek();
    }

    public  void stopSeekbar() {
        mFxGraphView.stopSeek();
    }
}
