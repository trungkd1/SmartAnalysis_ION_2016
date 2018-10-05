package jp.co.fujixerox.sa.ion.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;

import jp.co.fujixerox.sa.ion.R;

/**
 * Created by TrungKD
 */
public class VolumeDialogFragment extends DialogFragment {
    public static final String TAG = VolumeDialogFragment.class.getSimpleName();
    Activity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View contentView = inflater.inflate(R.layout.dialog_volumn, null);
        builder.setView(contentView);
        Dialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        final AudioManager audioManager = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
        SeekBar volumeControl = (SeekBar) contentView.findViewById(R.id.seekVolumn);
        volumeControl.setMax(audioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volumeControl.setProgress(audioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC));

        volumeControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Do nothing
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Do nothing

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        progress, 0);
            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);
        return dialog;
    }
}
