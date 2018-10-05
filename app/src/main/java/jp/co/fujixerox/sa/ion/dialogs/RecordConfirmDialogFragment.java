package jp.co.fujixerox.sa.ion.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Timer;
import java.util.TimerTask;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.db.ICloudParams;
import jp.co.fujixerox.sa.ion.utils.Utility;
import jp.co.fujixerox.sa.ion.views.MediaPlayerController;

import static jp.co.fujixerox.sa.ion.views.MediaPlayerController.AudioAction.PAUSE;
import static jp.co.fujixerox.sa.ion.views.MediaPlayerController.AudioAction.PLAY;
import static jp.co.fujixerox.sa.ion.views.MediaPlayerController.AudioAction.STOP;

/**
 * Dialog confirm recorded audio
 * Created by TrungKD
 */
public class RecordConfirmDialogFragment extends DialogFragment {
    public static final String TAG = DialogFragment.class.getSimpleName();
    private final int mSeekStep = Utility.TIME_INTERVAL_UPDATE_AUDIO_PROCESS;
    private HandleEventAfterFinishDialogListener listener;
    private boolean isPause = false;
    // Seek bar variables
    private float mCurrentSeekPosition;
    /**
     * Progress playing audio
     */
    private ProgressBar progressPlaying;
    /**
     * Timer for progress play audio
     */
    private Timer timer = new Timer();
    /**
     * audio file path
     */
    private String mFilePath;
    /**
     * MediaPlayer Controller to control play audio
     */
    private MediaPlayerController mMediaPlayerController;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View contentView = inflater.inflate(R.layout.dialog_after_record, null);
        builder.setView(contentView);
        Dialog dialog = builder.create();
        dialog.setCancelable(false);
        progressPlaying = (ProgressBar) contentView
                .findViewById(R.id.progress_after_recording);
        TextView txtExplainAfterRecording = (TextView) contentView
                .findViewById(R.id.txt_explain_after_recording);
        txtExplainAfterRecording.setText(R.string.explain_after_recording);
        //setup MediaPlayer view for control play audio
        setupMediaPlayer(contentView);
         // ImageButton click for show volume control
        ImageButton ibVolume = (ImageButton) contentView.findViewById(R.id.ib_volume);
        ibVolume.setOnClickListener(onClickListener);
        // Button click for record audio again
        View btnRecordAgain = contentView.findViewById(R.id.btn_record_again);
        // button click for go to Analysis screen
        View btnBeginAnalysis = contentView.findViewById(R.id.btn_begin_analysis);
        // button click for go to Report screen
        View btnCreateReport = contentView.findViewById(R.id.btn_create_report);
        btnCreateReport.setOnClickListener(onClickListener);
        btnBeginAnalysis.setOnClickListener(onClickListener);
        btnRecordAgain.setOnClickListener(onClickListener);
        dialog.setCanceledOnTouchOutside(false);

        //check perameter other for show dialog
        if(getArguments().getBoolean(ICloudParams.other)) {
            btnCreateReport.setVisibility(View.VISIBLE);
            btnBeginAnalysis.setVisibility(View.GONE);
        }else {
            btnCreateReport.setVisibility(View.GONE);
            btnBeginAnalysis.setVisibility(View.VISIBLE);
        }
        return dialog;
    }

    /**
     * Button click action listener
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.btn_record_again) {
                //dismiss dialog
                dismiss();
                //stop play audio
                stopPlayAudio();
                //start recording again
                if (listener != null) {
                    listener.onClickRecordAgain();
                }
            } else if (id == R.id.btn_begin_analysis) {
                //dismiss dialog
                dismiss();
                //stop play audio
                stopPlayAudio();
                //goto analysis screen
                if (listener != null) {
                    listener.onClickBeginAnalysis();
                }
            } else if (id == R.id.btn_create_report) {
                //dismiss dialog
                dismiss();
                //stop play audio
                stopPlayAudio();
                //create report
                if (listener != null) {
                    listener.onClickCreateReport();
                }
            } else if (id == R.id.ib_volume) {
                //open volume controls
                if (listener != null) {
                    listener.onClickVolume();
                }
            } else if (id == R.id.ib_play) {
                ToggleButton toggleButton = (ToggleButton) view;
                if (toggleButton.isChecked()) {
                    mMediaPlayerController.onClick(PLAY);
                } else {
                    mMediaPlayerController.onClick(PAUSE);
                }
            } else if (id == R.id.ib_stop) {
                mMediaPlayerController.onClick(STOP);
            }
        }
    };

    private void stopPlayAudio() {
        if (mMediaPlayerController != null) {
            mMediaPlayerController.release();
            stopSeek();
        }
    }

    /**
     * Init MediaPlayer view
     * @param contentView View
     */
    private void setupMediaPlayer(View contentView) {
        final ImageButton ibStop = (ImageButton) contentView.findViewById(R.id.ib_stop);
        ibStop.setOnClickListener(onClickListener);
        final ToggleButton ibPlay = (ToggleButton) contentView.findViewById(R.id.ib_play);
        ibPlay.setOnClickListener(onClickListener);
        final MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMediaPlayerController.onClick(STOP);
                        ibPlay.setChecked(false);
                        stopSeek();
                    }
                });
            }
        };



        /**
         * MediaPlayer callback object
         */
        MediaPlayerController.PlayCallback playPauseCallback = new MediaPlayerController.PlayCallback() {
            @Override
            public void onPlaying(MediaPlayer mediaPlayer) {
                int duration = mediaPlayer.getDuration();
                Log.d(TAG, "duration=" + duration);
                if (mediaPlayer.isPlaying()) {
                    startSeek(duration, onCompletionListener);

                }
            }

            @Override
            public void onPausing(MediaPlayer mediaPlayer) {
                pauseSeek();

            }

            @Override
            public void onStop(MediaPlayer mediaPlayer) {
                stopSeek();
                ibPlay.setChecked(false);
            }

        };
        mMediaPlayerController = new MediaPlayerController(getActivity(), mFilePath);
        mMediaPlayerController.setOnCompletedListener(playPauseCallback, onCompletionListener);
    }

    private void stopSeek() {
        isPause = false;
        if (timer != null) {
            timer.cancel();
        }
        mCurrentSeekPosition = 0;
        progressPlaying.setProgress(0);
    }

    private void pauseSeek() {
        isPause = true;
    }

    private void startSeek(final int duration, final MediaPlayer.OnCompletionListener callback) {
        progressPlaying.setMax(duration);
        if (isPause) {
            isPause = false; //resume
        } else {
            timer = new Timer("seek");
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    if (isPause) return;
                    progressPlaying.incrementProgressBy(mSeekStep);
                    mCurrentSeekPosition += mSeekStep;
                    if (mCurrentSeekPosition >= duration) {
                        mCurrentSeekPosition = 0;
                        cancel();
                        timer.cancel();
                        callback.onCompletion(null);
                    }
                }
            };
            timer.schedule(task, 0, mSeekStep);
        }
    }

    public void setListener(HandleEventAfterFinishDialogListener listener) {
        this.listener = listener;
    }

    public void setAudioFilePath(String filePath) {
        this.mFilePath = filePath;
    }

    /**
     * Interface for callback from view to activity
     */
    public interface HandleEventAfterFinishDialogListener {
        void onClickBeginAnalysis();

        void onClickCreateReport();

        void onClickVolume();

        void onClickRecordAgain();
    }

}