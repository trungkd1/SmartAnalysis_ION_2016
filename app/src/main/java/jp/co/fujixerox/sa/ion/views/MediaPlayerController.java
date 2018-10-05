package jp.co.fujixerox.sa.ion.views;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.Toast;

import java.io.IOException;

import jp.co.fujixerox.sa.ion.DefaultApplication;
import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.utils.CommonUtils;
import jp.co.fujixerox.sa.ion.utils.Utility;

import static android.media.MediaPlayer.OnCompletionListener;

/**
 * Created by TrungKD
 * Audio MediaPlayer controller.<br>
 * Send event from UI button (play, pause, stop) to MediaPlayer
 */
public class MediaPlayerController {
    private static final String TAG = MediaPlayerController.class.getSimpleName();
    public enum AudioAction {PLAY, STOP, PAUSE}
    private MediaPlayer mMediaPlayer;
    private Context mContext;
    private String mAudioPath;
    private PlayCallback mPlayPauseCallback;

    public MediaPlayerController(Context context, String audioPath) {
        this.mContext = context;
        this.mAudioPath = audioPath;
        prepare();
    }

    public void setOnCompletedListener(PlayCallback playPauseCallback, OnCompletionListener onCompletionListener) {
        mMediaPlayer.setOnCompletionListener(onCompletionListener);
        mPlayPauseCallback = playPauseCallback;
    }
    public void setOnCompletedListener(OnCompletionListener onCompletionListener) {
        mMediaPlayer.setOnCompletionListener(onCompletionListener);
    }


    public void changeAudio(String audioPath) {
        this.mAudioPath = audioPath;
        release();
        prepare();
    }

    /**
     * Reset and release MediaPlayer<br>
     * Must be call this method at Activity#onStop()
     */
    public void release() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void onClick(AudioAction action) {
        if (mMediaPlayer == null) return;
        boolean playing = mMediaPlayer.isPlaying();
        if (action == AudioAction.PAUSE && playing) {
            //Tracking Event
            /* DefaultApplication.getInstance().trackEvent(
                    mContext.getString(R.string.category_ui_event),
                    mContext.getString(R.string.action_button_press),
                    mContext.getString(R.string.label_pause)); 160407 mit */
            mMediaPlayer.pause();
            if (mPlayPauseCallback != null) {
                mPlayPauseCallback.onPausing(mMediaPlayer);
            }
        } else if (action == AudioAction.PLAY) {
            //Tracking Event
            DefaultApplication.getInstance().trackEvent(
                    TAG +":"+ mContext.getString(R.string.action_button_press),
                    mContext.getString(R.string.label_play),
                    CommonUtils.getStringPreferences(mContext, Utility.SHARE_PREFERENCES.KEY_HASH_SELECTED));
            mMediaPlayer.start();
            if (mPlayPauseCallback != null) {
                mPlayPauseCallback.onPlaying(mMediaPlayer);
            }
        } else if (action == AudioAction.STOP) {
            /*} DefaultApplication.getInstance().trackEvent(
                    mContext.getString(R.string.category_ui_event),
                    mContext.getString(R.string.action_button_press),
                    mContext.getString(R.string.label_stop)); 160407 mit */
            if (playing) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.reset();
            if (mPlayPauseCallback != null) {
                mPlayPauseCallback.onStop(mMediaPlayer);
            }
            prepare();
        }
    }

    private void prepare() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        if (mAudioPath == null) {
            return;
        }
        try {
            mMediaPlayer.setDataSource(mContext, Uri.parse(mAudioPath));
            mMediaPlayer.prepare();
        } catch (IllegalArgumentException e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (SecurityException e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (IllegalStateException e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public interface PlayCallback {
        void onPlaying(MediaPlayer mediaPlayer);
        void onPausing(MediaPlayer mediaPlayer);
        void onStop(MediaPlayer mediaPlayer);
    }
}
