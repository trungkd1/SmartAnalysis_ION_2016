package jp.co.fujixerox.sa.ion.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.List;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.entities.Item;
import jp.co.fujixerox.sa.ion.entities.Value;
import jp.co.fujixerox.sa.ion.db.AudioFormData;

/**
 * Created by TrungKD
 */
public class RadioButtonItemView extends AbstractItemView {
    /**
     * Editable or selectable view
     */
    protected RadioGroup editableView;

    public RadioButtonItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RadioButtonItemView(Context context) {
        super(context, null);
    }

    @Override
    public void initView() {
        editableView = (RadioGroup) findViewById(R.id.radioGroupItem);
        tvTitle = (TextView) findViewById( R.id.textViewItemLabel);
        ivValidateIcon = (ImageView) findViewById(R.id.ivRadioChecked);
        childrenLayout = (LinearLayout) findViewById(R.id.childLayout);

    }

    @Override
    public void setItem(final Item item) {
        super.setItem(item);
        tvTitle.setText(item.getLabelForView());
        editableView.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = (RadioButton) editableView.findViewById(checkedId);
                int index = editableView.indexOfChild(radioButton);
                List<Value> values = item.getListvalue();
                Value value = values.get(index);
                //handle check all item valid
                onValueChangedListener.onValueChanged(item.getFormid(), value);
            }
        });
    }

    @Override
    public AudioFormData createAudioFormData() {
        AudioFormData data = new AudioFormData();
        data.setFormid(item.getFormid());
        int id = editableView.getCheckedRadioButtonId();
        if (id > 0) {
            RadioButton radioButton = (RadioButton) editableView.findViewById(id);
            int index = editableView.indexOfChild(radioButton);
            List<Value> values = item.getListvalue();
            Value value = values.get(index);
            data.setText(value.getText());
            data.setValue(value.getValue());
        }
        return data;
    }

    @Override
    public void setValue(String value) {
       List<Value> values = item.getListvalue();
        int count = editableView.getChildCount();
        for (int i = 0; i < count; i++) {
            if (values.get(i).getValue().equalsIgnoreCase(value)) {
                RadioButton radioButton = (RadioButton) editableView.getChildAt(i);
                radioButton.setChecked(true);
                break;
            }
        }
    }
}