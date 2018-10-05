package jp.co.fujixerox.sa.ion.views;

import android.content.Context;
import android.nfc.Tag;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.db.AudioData;
import jp.co.fujixerox.sa.ion.db.AudioFormData;
import jp.co.fujixerox.sa.ion.db.ICloudParams;
import jp.co.fujixerox.sa.ion.entities.Item;
import jp.co.fujixerox.sa.ion.utils.JsonParser;

/**
 * Created by FPT on 9/7/16.
 */
public class CheckItemView extends AbstractItemView {

    private LinearLayout mLayoutDetail;

    private ArrayList<String> mCheckInputList= new ArrayList<String>();

    private String mFormId;

    private ArrayList<String> mInputFormItemList= new ArrayList<String>();

    public CheckItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckItemView(Context context) {
        super(context, null);
    }


    @Override
    public void initView() {
        tvTitle = (TextView) findViewById(R.id.textViewItemLabel);


    }

    @Override
    public void setItem(Item item) {
        super.setItem(item);
        tvTitle.setText(item.getLabelForView());

    }

    @Override
    public void setValue(String checkedValueJson) {

    }

    @Override
    public AudioFormData createAudioFormData() {
        AudioFormData data = new AudioFormData();
        String causesJson = JsonParser.makeJsonStringArray(mCheckInputList);
        data.setValue(causesJson);
        data.setFormid(item.getFormid());
        data.setText(causesJson);
        return data;
    }

    public void setTreatmentView(String formid,String checkedValueJson, String jsonForm ) {
        mFormId = formid;
        mInputFormItemList = JsonParser.makeArrayListOfString(jsonForm);
        if (mInputFormItemList != null && !mInputFormItemList.isEmpty()) {
            //set Layout checkview
            mLayoutDetail = (LinearLayout) findViewById(R.id.layout_parts_detail);
            if(mInputFormItemList.indexOf(getContext().getString(R.string.other))< 0){
                mInputFormItemList.add(getContext().getString(R.string.other));
            }
        }
        List<String> checkedValueList = new ArrayList<>();
        if (checkedValueJson != null) {

            checkedValueList = JsonParser.makeArrayListOfString(checkedValueJson);
        }

        for (String value : mInputFormItemList) {
            CheckBox checkBox = createCheckbox(value, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        mCheckInputList.add(buttonView.getText().toString());
                    } else {
                        mCheckInputList.remove(buttonView.getText().toString());
                    }

                    setTreatmentResult(mFormId);
                }
            });
            mLayoutDetail.addView(checkBox);
            if (checkedValueList.contains(value)) {
                checkBox.setChecked(true);
            }
        }
    }


    /**
     * Get form data form treatment result
     *
     * @return List<AudioFormData>
     */
    private void setTreatmentResult(String JsonType) {
        String causesJson = null;
        if (audioData!= null) {
            if(ICloudParams.cause.equals(mFormId)){
                causesJson = JsonParser.makeJsonStringArray(mCheckInputList);
                audioData.setCause(causesJson);
            }else if(ICloudParams.method.equals(mFormId)){
                causesJson = JsonParser.makeJsonStringArray(mCheckInputList);
                audioData.setMethod(causesJson);
            }
        }
    }

    private CheckBox createCheckbox(String text,
                                    CompoundButton.OnCheckedChangeListener checkedChangeListener) {
        CheckBox checkBox = new CheckBox(getContext());
        checkBox.setOnCheckedChangeListener(checkedChangeListener);
        checkBox.setText(text);
        return checkBox;
    }

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener =
            new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        mCheckInputList.add(buttonView.getText().toString());
                    } else {
                        mCheckInputList.remove(buttonView.getText().toString());
                    }
                    setTreatmentResult(mFormId);
                }
            };

}
