package jp.co.fujixerox.sa.ion.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.utils.CommonUtils;
import jp.co.fujixerox.sa.ion.utils.Utility;

/**
 * Setting dialog open from Analysis Screen
 * Created by TrungKD
 */
public class SettingDialogFragment extends DialogFragment {
    private static final String TAG = "SettingDialogFragment";
    private int mBeforeValueFFTSize, mBeforeValueAnalysisTime, mBeforeValueStepWidth;
    private int mValueFFTSize, mValueAnalysisTime, mValueStepWidth;
    private int indexFFTSize, indexAnalysisTime, indexStepWidth;
    private ActivityCallback mActivityCallback;
    private SharedPreferences.Editor mEditor;

    public void setActivityCallback(ActivityCallback callback) {
        this.mActivityCallback = callback;
    }

    public void setCurrentSetting(int fft_size, int step_width, int analysis_time, SharedPreferences.Editor editor) {
        this.mBeforeValueFFTSize = fft_size;
        this.mBeforeValueAnalysisTime = analysis_time;
        this.mBeforeValueStepWidth = step_width;
        this.mEditor = editor;
        for (int i = 0; i < Utility.FFT_VALUES.length; i++) {
            if (Utility.FFT_VALUES[i] == fft_size) {
                indexFFTSize = i;
                this.mValueFFTSize = fft_size;
                break;
            }
        }
        for (int i = 0; i < Utility.STEP_WIDTH_VALUES.length; i++) {
            if (Utility.STEP_WIDTH_VALUES[i] == step_width) {
                indexStepWidth = i;
                this.mValueStepWidth = step_width;
                break;
            }
        }
        for (int i = 0; i < Utility.ANALYSIS_TIME_VALUES.length; i++) {
            if (Utility.ANALYSIS_TIME_VALUES[i] == analysis_time) {
                indexAnalysisTime = i;
                this.mValueAnalysisTime = analysis_time;
                break;
            }
        }
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View contentView = inflater.inflate(R.layout.dialog_setting, null);
        builder.setView(contentView);
        Dialog dialog = builder.create();
        dialog.setCancelable(false);

        RadioGroup rgFFT = (RadioGroup) contentView.findViewById(R.id.rgFFT);
        RadioButton rbFFT = (RadioButton) rgFFT.getChildAt(indexFFTSize);
        rbFFT.setChecked(true);
        RadioGroup rgStep = (RadioGroup) contentView.findViewById(R.id.rgStep);
        RadioButton rbStep = (RadioButton) rgStep.getChildAt(indexStepWidth);
        rbStep.setChecked(true);
        RadioGroup rgTime = (RadioGroup) contentView.findViewById(R.id.rgTime);
        RadioButton rbTime = (RadioButton) rgTime.getChildAt(indexAnalysisTime);
        rbTime.setChecked(true);

        rgFFT.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rbFFT = (RadioButton) group.findViewById(checkedId);
                mValueFFTSize = Integer.valueOf(rbFFT.getText().toString());

            }
        });

        rgStep.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rbStep = (RadioButton) group.findViewById(checkedId);
                mValueStepWidth = Integer.valueOf(rbStep.getText().toString());
            }
        });

        rgTime.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rbTime = (RadioButton) group.findViewById(checkedId);
             mValueAnalysisTime = Integer.parseInt(rbTime.getText().toString());
            }
        });

        Button saveSetting = (Button) contentView.findViewById(R.id.btn_saveSetting);
        saveSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtils.savePreferences(mValueStepWidth, mValueFFTSize,
                        mValueAnalysisTime, mEditor);
                dismiss();
                if (mActivityCallback != null) {
                    mActivityCallback.onSettingChanged();
                    if (mBeforeValueAnalysisTime != mValueAnalysisTime) {
                        mActivityCallback.onAnalysisTimeChanged();
                    }
                }
            }
        });

        Button cancelSetting = (Button) contentView.findViewById(R.id.btn_cancelSetting);
        cancelSetting.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
                //do nothing
            }
        });
        return dialog;
    }

    public interface ActivityCallback {
        void onSettingChanged();
        void onAnalysisTimeChanged();
    }

}
