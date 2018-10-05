package jp.co.fujixerox.sa.ion.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import jp.co.fujixerox.sa.ion.DefaultApplication;
import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.dialogs.VolumeDialogFragment;
import jp.co.fujixerox.sa.ion.utils.CommonUtils;
import jp.co.fujixerox.sa.ion.utils.Utility;

public abstract class AbstractFragmentActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Check device is Tablet or Not
     * @return true is tablet and otherwise
     */
    public boolean isTablet() {
        boolean xlarge = ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE);
        boolean large = ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
        return (xlarge || large);
    }

    /**
     * Button volume click event
     *
     * @param v View
     */
    public void onClickVolume(View v) {
        VolumeDialogFragment dialogFragment = new VolumeDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "volume dialog");
        //Tracking Event
        DefaultApplication.getInstance().trackEvent(
                getString(R.string.action_show_dialog),
                getString(R.string.volume),
                CommonUtils.getStringPreferences(this,Utility.SHARE_PREFERENCES.KEY_HASH_SELECTED)
        );
    }

    /**
     * Send tracking event button press
     * @param v
     */
    protected void sendTrackingButtonPress(Button v) {
        //Tracking Event
        DefaultApplication.getInstance().trackEvent(
                getString(R.string.action_button_press),
                v.getText().toString(),
                CommonUtils.getStringPreferences(this, Utility.SHARE_PREFERENCES.KEY_HASH_SELECTED)
                );
    }

    /**
     * Send tracking event button press
     * @param label
     */
    protected void sendTrackingButtonPress(String label) {
        //Tracking Event
        DefaultApplication.getInstance().trackEvent(
                getString(R.string.action_button_press),
                label,
                CommonUtils.getStringPreferences(this,Utility.SHARE_PREFERENCES.KEY_HASH_SELECTED)
        );
    }

    /**
     * Cancel all background task thread
     */
    protected void cancelAllBackgroundTask() {
        //should be override in child class
    }

    //add accountid 160404 mit

    final private static String realm = "5H7XnYmVP258SFXZ";

    protected void sendTrackingStartUP(Context context){
        String act =  CommonUtils.getStringPreferences(context, Utility.SHARE_PREFERENCES.KEY_HASH_SELECTED);

        //Tracking Event
        DefaultApplication.getInstance().trackEvent(
                getString(R.string.category_ui_event),
                "Startup",
                act
        );
    }
    protected void sendTrackingRecording(Context context, boolean rerecord){
        String rerec = "";
        if(rerecord == true) rerec = " Rerec";
        DefaultApplication.getInstance().trackEvent(
                getString(R.string.category_ui_event),
                "Recording Input Screen" + rerec,
                CommonUtils.getStringPreferences(context, Utility.SHARE_PREFERENCES.KEY_HASH_SELECTED)
        );
    }

    protected void sendTrackingAnalysis(Context context, boolean fromTop){
        String act = CommonUtils.getStringPreferences(context, Utility.SHARE_PREFERENCES.KEY_HASH_SELECTED);
        String top = "";
        if(fromTop == true) top = " from TopPage";
        // Tracking Event
        DefaultApplication.getInstance().trackEvent(
                getString(R.string.category_ui_event),
                "Analysis" + top,
                act
        );
    }
    protected void sendTrackingCatalog(Context context, boolean selectMode){
        String act = CommonUtils.getStringPreferences(context, Utility.SHARE_PREFERENCES.KEY_HASH_SELECTED);
        String mode;
        if(selectMode == true) mode = " SelectMode";
        else mode = " ListMode";
        //Tracking Event
        DefaultApplication.getInstance().trackEvent(
                getString(R.string.category_ui_event),
                "Catalog"+mode,
                act
        );
    }
    protected void sendTrackingCompare(Context context, boolean fromTop){
        String act = CommonUtils.getStringPreferences(context, Utility.SHARE_PREFERENCES.KEY_HASH_SELECTED);
        String from="";
        if(fromTop == true) from="from Toppage";
        //Tracking Event
        DefaultApplication.getInstance().trackEvent(
                "UI Event",
                "Compare :" + from,
                act
        );
    }
    protected void sendTrackingSetting(Context context){
        String act = CommonUtils.getStringPreferences(context, Utility.SHARE_PREFERENCES.KEY_HASH_SELECTED);
        //TrackingEvent
        DefaultApplication.getInstance().trackEvent(
                "UI Event",
                "Setting",
                act
        );
    }

    protected void sendTrackingDevice(Context context){
        String act = CommonUtils.getStringPreferences(context, Utility.SHARE_PREFERENCES.KEY_HASH_SELECTED);
        String ver = getString(R.string.version_name);
        String device = Build.MODEL;
        DefaultApplication.getInstance().trackEvent(
                "DeviceInfo",
                ver + ":" + device,
                act);
    }

    //MD5 160404 mit
    final private static String digest(String str){
        String ret = "";
        MessageDigest md;
        try{
            md = MessageDigest.getInstance("MD5");
        }catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        md.update(str.getBytes());
        byte[] hash = md.digest();
        for(int i=0; i < hash.length; i++){
            int d = hash[i];
            if(d < 0){
                d += 256;
            }
            if(d < 16){
                ret += "0";
            }
            ret += Integer.toString(d, 16);
        }
        return ret;
    }
    protected String getCurrentSelectAccount(Context context){
        return CommonUtils.getStringPreferences(context, Utility.SHARE_PREFERENCES.KEY_HASH_SELECTED);

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelAllBackgroundTask();
    }
}
