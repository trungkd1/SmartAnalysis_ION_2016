package jp.co.fujixerox.sa.ion.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.db.AudioData;
import jp.co.fujixerox.sa.ion.db.AudioFormData;
import jp.co.fujixerox.sa.ion.entities.Item;
import jp.co.fujixerox.sa.ion.entities.Value;
import jp.co.fujixerox.sa.ion.utils.JsonParser;
import jp.co.fujixerox.sa.ion.utils.Utility;

/**
 * Created by TrungKD
 */
public class Radio4ButtonItemView extends AbstractItemView{

    private static final String TAG = Radio4ButtonItemView.class.getSimpleName();
    /**
     * 処置結果 RadioGroup
     */
    protected RadioGroup radioGroup;
    protected RadioButton radioBtn1;
    protected RadioButton radioBtn2;
    protected RadioButton radioBtn3;
    protected RadioButton radioBtn4;
    protected String textValue;

    public Radio4ButtonItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Radio4ButtonItemView(Context context) {
        super(context, null);
    }

    @Override
    public void initView() {
        radioGroup =
                (RadioGroup) findViewById(R.id.rg_result);
        radioBtn1 =  ((RadioButton) radioGroup.findViewById(R.id.radioNot));
        radioBtn2 =  ((RadioButton) radioGroup.findViewById(R.id.radioDone));
        radioBtn3 =  ((RadioButton) radioGroup.findViewById(R.id.radioNonRecurring));
        radioBtn4 =  ((RadioButton) radioGroup.findViewById(R.id.radioUnknown));
        radioBtn1.setOnClickListener(click );
        radioBtn2.setOnClickListener(click );
        radioBtn3.setOnClickListener(click );
        radioBtn4.setOnClickListener(click );

    }

    private OnClickListener click = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view instanceof RadioButton) {
                RadioButton radioButton = (RadioButton) view;
                Object tag = radioGroup.getTag();
                int lastCheckedId = -1;
                if (tag != null) {
                    lastCheckedId = (int) tag;
                }
                if (lastCheckedId == view.getId()) {
                    radioGroup.clearCheck();
                    radioGroup.setTag(null);
                } else {
                    radioGroup.setTag(radioGroup.getCheckedRadioButtonId());
                }
            }
            RadioButton radioButton = (RadioButton)findViewById(view.getId());
            textValue = radioButton.getText().toString();

        }
    };

    /**
     * Set check/unchecked for radio button
     *
     */
    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(l);
    }

    @Override
    public AudioFormData createAudioFormData() {

        AudioFormData data = new AudioFormData();
        data.setFormid(item.getFormid());
        int id = radioGroup.getCheckedRadioButtonId();

        if (id == R.id.radioDone) {
            data.setValue(Utility.RESULT_KEY.OK);
        } else if (id == R.id.radioNot) {
            data.setValue(Utility.RESULT_KEY.NG);
        } else if (id == R.id.radioNonRecurring) {
            data.setValue(Utility.RESULT_KEY.NON_RECURRING);
        } else if (id == R.id.radioUnknown) {
            data.setValue(Utility.RESULT_KEY.UNKNOWN);
        } else {
            data.setValue(null);
        }

        data.setText(textValue);

        return data;
    }

    @Override
    public void setValue(String value) {
        if (Utility.RESULT_KEY.NG.equals(value)) {
            radioBtn1.setChecked(true);
            radioGroup.setTag(R.id.radioNot);
        } else if (Utility.RESULT_KEY.OK.equals(value)) {
            radioBtn2.setChecked(true);
            radioGroup.setTag(R.id.radioDone);
        } else if (Utility.RESULT_KEY.NON_RECURRING.equals(value)) {
            radioBtn3.setChecked(true);
            radioGroup.setTag(R.id.radioNonRecurring);
        } else if (Utility.RESULT_KEY.UNKNOWN.equals(value)) {
            radioBtn4.setChecked(true);
            radioGroup.setTag(R.id.radioUnknown);
        }
    }
}