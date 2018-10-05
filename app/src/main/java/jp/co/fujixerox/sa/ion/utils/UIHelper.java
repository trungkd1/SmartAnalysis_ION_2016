package jp.co.fujixerox.sa.ion.utils;

import android.os.SystemClock;
import android.util.Log;

/**
 * Created by TrungKD
 */
public class UIHelper {
    /**
     * Click event delay time (ms)
     */
    private static final long CLICK_DELAY = 1000;
    private static String TAG = UIHelper.class.getSimpleName();
    /**
     * Previous click event execution time (ms)
     */
    private static long mOldClickTime = 0L;

    /**
     * Click event I determine whether it is possible run
     *
     * @return Executability of the click event (true: Yes , false: whether )
     */
    public static boolean canExecuteClickEvent() {
        // Get the current time
        long time = SystemClock.elapsedRealtime();
        Log.d(TAG, "Check canExecuteClickEvent: " + mOldClickTime + "|" + time);
        // If not, a certain amount of time click event can not be executed
        if (time - mOldClickTime < CLICK_DELAY) {
            Log.d(TAG, "canExecuteClickEvent return false");
            return false;
        }

        //Click event can be run after a short period of
        mOldClickTime = time;
        Log.d(TAG, "canExecuteClickEvent return true");
        return true;
    }
}
