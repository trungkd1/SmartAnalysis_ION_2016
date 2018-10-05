package jp.co.fujixerox.sa.ion.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.example.ionanalyzelib.IonAnalyzeLib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;

public class CommonUtils {
    private static final String TAG = CommonUtils.class.getSimpleName();
    private static String USER_AGENT = null;
    /**
     * Load Preferences
     *
     * @param key
     * @param context
     * @return
     */
    public static int loadPreferences(String key, Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(
                Utility.SHARE_PREFERENCES.MY_PREFERENCES, Context.MODE_PRIVATE);
        return sharedpreferences.getInt(key, 0);
    }

    /**
     * Load Preferences
     *
     * @param key
     * @param context
     * @return
     */
    public static int loadIntPreferences(String key, Context context, int defaultValue) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(
                Utility.SHARE_PREFERENCES.MY_PREFERENCES, Context.MODE_PRIVATE);
        return sharedpreferences.getInt(key, defaultValue);
    }

    /**
     * Save Preferences
     *
     * @param selectedStep
     * @param selectedFFT
     * @param selectedTime
     * @param editor
     */
    public static void savePreferences(int selectedStep, int selectedFFT,
                                       int selectedTime, Editor editor) {
        editor.putInt(Utility.SHARE_PREFERENCES.KEY_STEP_WIDTH, selectedStep);
        editor.putInt(Utility.SHARE_PREFERENCES.KEY_FFT_SIZE, selectedFFT);
        editor.putInt(Utility.SHARE_PREFERENCES.KEY_ANALYSIS_TIME, selectedTime);
        editor.apply();
    }

    /**
     * save string preferences
     * @param key: preference key
     * @param value: preference value
     */
    public static void saveStringPreferences(Context context, String key, String value) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(
                Utility.SHARE_PREFERENCES.MY_PREFERENCES, Context.MODE_PRIVATE);
        Editor editor = sharedpreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * get string preferences
     * @param context: Context
     * @param key: key preferences
     * @return value preferences
     */
    public static String getStringPreferences(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Utility.SHARE_PREFERENCES.MY_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, Utility.EMPTY_STRING);
    }

    /**
     * put pending audios id to list audios id that upload failed
     * @param context
     * @param value
     */
    public static void putAudiosIdToListAudiosPending(Context context, String value) {
        String lstAudiosIdValue = getStringPreferences(context, Utility.SHARE_PREFERENCES.KEY_REPORTS_UPLOAD_PENDING);
        if (checkValueHasExistInAudiosId(lstAudiosIdValue, value)) {
            return;
        }
        StringBuilder stringBuilder = new StringBuilder(lstAudiosIdValue);
        stringBuilder.append(value).append(Utility.CHARACTERS_SEPARATE);
        lstAudiosIdValue = stringBuilder.toString();
        saveStringPreferences(context, Utility.SHARE_PREFERENCES.KEY_REPORTS_UPLOAD_PENDING, lstAudiosIdValue);
    }

    private static boolean checkValueHasExistInAudiosId(String audiosIdString, String value) {
        if (TextUtils.isEmpty(audiosIdString)) {
            return false;
        }
        return audiosIdString.contains(value);
    }

    /**
     * remove audios id out of list audios id that upload failed
     * @param context: Context
     * @param value: audios id value
     */
    public static void removeAudiosIdOutListAudiosPending(Context context, String value) {
        String lstAudiosIdValue = getStringPreferences(context, Utility.SHARE_PREFERENCES.KEY_REPORTS_UPLOAD_PENDING);
        if (TextUtils.isEmpty(lstAudiosIdValue)) {
            return;
        }
        //check if audios id isn't in list audios id string when return
        if (lstAudiosIdValue.indexOf(value) < 0) {
            return;
        }
        String[] lstAudiosId = lstAudiosIdValue.split(Utility.CHARACTERS_SEPARATE);
        if (lstAudiosId == null || lstAudiosId.length == 0) {
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (String audiosId : lstAudiosId) {
            Log.i(TAG, "@@@AUDIOS ID: " + audiosId);
            if (!TextUtils.equals(audiosId, value)) {
                stringBuilder.append(audiosId).append(Utility.CHARACTERS_SEPARATE);
            }
        }
        lstAudiosIdValue = stringBuilder.toString();
        saveStringPreferences(context, Utility.SHARE_PREFERENCES.KEY_REPORTS_UPLOAD_PENDING, lstAudiosIdValue);
    }

    /**
     * decode file to bitmap with correct scale value
     *
     * @param f
     * @return
     */
    public static Bitmap decodeFile(File f) {
        try {
            // decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            FileInputStream stream1 = new FileInputStream(f);
            BitmapFactory.decodeStream(stream1, null, o);
            stream1.close();

            // Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE = 160;
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE
                        || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            FileInputStream stream2 = new FileInputStream(f);
            Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, o2);
            stream2.close();
            return bitmap;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "decodeFile FileNotFoundException", e);
        } catch (IOException e) {
            Log.e(TAG, "decodeFile IOException", e);
        }
        return null;
    }

    /**
     * decode inputStream to bitmap
     *
     * @param in
     * @return
     */
    public static Bitmap decodeStream(InputStream in) {
        Bitmap bitmap = null;
        if (in == null) {
            Log.e(TAG, "decodeStream null");
            return null;
        }
        try {
            Log.i(TAG, "@@@Begin decodeStream");
            // decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);
            // Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE = 400;
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE
                        || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            bitmap = BitmapFactory.decodeStream(in, null, o2);
        } catch (Exception e) {
            Log.e(TAG, "@@Error when decode image", e);
        }finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    /**
     * get bitmap from file image in asset
     *
     * @param assetPath
     * @param fileName
     * @return
     */
    public static Bitmap getBitmapFromAsset(String assetPath, String fileName, AssetManager assetManager) {
        InputStream in = FileUtils.readStreamFromAsset(assetPath, fileName, assetManager);
        return decodeStream(in);
    }

    /**
     * convert date to string in pattern
     *
     * @param date
     * @param datePattern
     * @return
     */
    public static String convertDateToString(Long date, String datePattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
            return sdf.format(date);
        } catch (Exception ex) {
            Log.e(TAG, "Error when format date: ", ex);
            return null;
        }
    }

    /**
     * Use STFT library to analys noise audio file
     *
     * @return bitmap
     */
    public static Bitmap analysisAudio(String filename, IonAnalyzeLib ionAnalyzeLib, int mCurrentFFT, int mCurrentTimeAnalysis,
                                       int mCurrentStepWidth) {
        Bitmap result = null;
        // DUMMY data
        Log.v(TAG, "ANALYSIS_SCREEN AUDIO: " + filename);
        try {
            result = ionAnalyzeLib.AnalyzeWaveFile(filename, mCurrentFFT,
                    mCurrentStepWidth, mCurrentTimeAnalysis);
            if (result == null) {
                Log.v(TAG, "ANALYSIS_SCREEN AUDIO RETURN NULL");
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error when analyze wave file ", ex);
        }
        return result;
    }

    /**
     * get setting info has save in preference
     *
     * @param context
     * @return
     */
    public static SettingParams getSettingParameters(Context context) {
        int selectedFFT = CommonUtils.loadPreferences(
                Utility.SHARE_PREFERENCES.KEY_FFT_SIZE,
                context);
        int fft = Utility.FFT_VALUES[selectedFFT];
        int selectedStep = CommonUtils.loadPreferences(
                Utility.SHARE_PREFERENCES.KEY_STEP_WIDTH,
                context);
        int stepWidth = Utility.STEP_WIDTH_VALUES[selectedStep];
        int selectedTime = CommonUtils.loadIntPreferences(
                Utility.SHARE_PREFERENCES.KEY_ANALYSIS_TIME,
                context, 2);
        int timeAnalysis = Utility.ANALYSIS_TIME_VALUES[selectedTime];
        return new SettingParams(fft, timeAnalysis, stepWidth);
    }

    /**
     * Create a user-agent text if not exist
     *
     * @return user-agent
     */
    public static String generateUserAgent() {
        if (USER_AGENT == null) {
            USER_AGENT = Utility.USER_AGENT_VALUE
                    .replace("{version}", jp.co.fujixerox.sa.ion.BuildConfig.VERSION_NAME)
                    .replace("{android_version}",
                            android.os.Build.VERSION.SDK_INT + "")
                    .replace("{manufacturer}", android.os.Build.MANUFACTURER)
                    .replace("{model}", android.os.Build.MODEL);
        }
        Log.v(TAG, "USER AGENT: " + USER_AGENT);
        return USER_AGENT;
    }

    /**
     * decode bitmap from image file
     *
     * @param imageFilePath
     * @return
     */
    public static Bitmap getBitmapFromImageFilePath(String imageFilePath) {
        Bitmap bitmap = null;
        // DUMMY data
        Log.v(TAG, "getBitmapFromImageFilePath: " + imageFilePath);
        File f = new File(imageFilePath);
        if (!f.exists()) {
            return bitmap;
        }
        try {
            // decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            FileInputStream stream1 = new FileInputStream(f);
            BitmapFactory.decodeStream(stream1, null, o);
            stream1.close();

            // Find the correct scale value. It should be the power of 2.
            final int REQUIRED_WIDTH_SIZE = 200;
            final int REQUIRED_HEIGHT_SIZE = 128;
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_WIDTH_SIZE
                        || height_tmp / 2 < REQUIRED_HEIGHT_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }
            // decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            FileInputStream stream2 = new FileInputStream(f);
            bitmap = BitmapFactory.decodeStream(stream2, null, o2);
            stream2.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Error when decode file image to bitmap", e);
        } catch (IOException e) {
            Log.e(TAG, "Error when decode file image to bitmap", e);
        }
        return bitmap;
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float density = resources.getDisplayMetrics().density;
        Log.v(TAG, "Density: " + density);
        return dp * (metrics.densityDpi / 160f);
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return px / (metrics.densityDpi / 160f);
    }

    /**
     *
     */
    //add accountid 160404 mit
    public static final String PREFS_AUTH_ACCOUNT = "auth_account";
    final private static String realm = "5H7XnYmVP258SFXZ";

    //MD5 160404 mit
    final public static String digest(String str){
        String ret = "";
        MessageDigest md;

        str = str + ":" + realm;
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

    /**
     * Compare two strings in Java with possible null values
     * @param str1
     * @param str2
     * @return
     */
    public static boolean compare(String str1, String str2) {
        boolean isEmpty1 = TextUtils.isEmpty(str1);
        boolean isEmpty2 = TextUtils.isEmpty(str2);
        if (isEmpty1 && isEmpty2) {
            return true;
        } else {
            return (str1 == null ? str2 == null : str1.equals(str2));
        }
    }

    /**
     * Show toast message
     * @param context context to use
     * @param msgRes message got from resource
     */
    public static void showToast(Context context, int msgRes){
        Toast.makeText(context, msgRes, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context context, int msgRes, int gravity){
        Toast toast = Toast.makeText(context, msgRes, Toast.LENGTH_SHORT);
        toast.setGravity(gravity, 0, 0);
        toast.show();
    }
}
