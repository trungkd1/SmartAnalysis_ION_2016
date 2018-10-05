package jp.co.fujixerox.sa.ion.fragments;

import android.support.v4.app.Fragment;

/**
 * Created by fxstdpc-admin on 2015/12/01.
 */
public abstract class AbstractFragment extends Fragment {

    /**
     * Cancel all background task thread
     */
    protected void cancelAllBackgroundTask() {
        //should be override in child class
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelAllBackgroundTask();
    }


}
