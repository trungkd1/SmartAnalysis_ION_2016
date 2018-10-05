package jp.co.fujixerox.sa.ion.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by TrungKD
 * A common OK/Cancel confirm dialog
 */
public class ConfirmDialogFragment extends DialogFragment {
    private OnButtonClickListener mListener;
    private String mTitle;
    private String mMessage;
    public void setOnButtonClickListener(OnButtonClickListener listener) {
        this.mListener = listener;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public void setMessage(String message) {
        this.mMessage = message;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog dialog = new AlertDialog.Builder(
                getActivity())
        .setTitle(mTitle)
        .setMessage(mMessage)
        .setCancelable(false)
        .setPositiveButton(
                getResources().getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (mListener != null) {
                            mListener.onOK();
                        }
                    }

                })
        .setNegativeButton(getResources().getString(android.R.string.cancel),
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
        void onOK();
        void onCancel();
    }
}
