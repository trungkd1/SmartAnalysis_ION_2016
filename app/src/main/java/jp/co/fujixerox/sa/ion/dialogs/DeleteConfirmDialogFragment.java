package jp.co.fujixerox.sa.ion.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.db.AudioData;
import jp.co.fujixerox.sa.ion.db.ICloudParams;

/**
 * Created by TrungKD
 */
public class DeleteConfirmDialogFragment extends DialogFragment {
    private OnButtonClickListener mListener;
    private AudioData mAudioData;
    public void setOnButtonClickListener(OnButtonClickListener listener) {
        this.mListener = listener;
    }

    public void setAudioData(AudioData audioData) {
        this.mAudioData = audioData;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog dialog = new AlertDialog.Builder(
                getActivity())
        .setTitle(R.string.delete_report_alert)
        .setMessage(getResources()
                .getString(R.string.delete_report_mess, mAudioData.getValueByFormId(ICloudParams.serialid)))
        .setCancelable(false)
        .setPositiveButton(
                getResources().getString(R.string.yes),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (mListener != null) {
                            mListener.onDelete();
                        }
                    }

                })
        .setNegativeButton(getResources().getString(R.string.no),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (mListener != null) {
                            mListener.onCancel();
                        }
                    }

                }).create();

        return dialog;
    }

    public interface OnButtonClickListener {
        void onDelete();
        void onCancel();
    }
}
