package jp.co.fujixerox.sa.ion.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.adapters.SpinnerArrayAdapter;
import jp.co.fujixerox.sa.ion.entities.Item;
import jp.co.fujixerox.sa.ion.entities.Value;
import jp.co.fujixerox.sa.ion.db.AudioFormData;
import jp.co.fujixerox.sa.ion.utils.Utility;

/**
 * Created by FPT on 2015/11/06.
 */
public class SpinnerItemView extends AbstractItemView implements OnItemSelectedListener {
    private static final String TAG = SpinnerItemView.class.getSimpleName();
    /**
     * Editable or selectable view
     */
    protected Spinner spinnerViewItem;
    private SpinnerArrayAdapter spinArrayAdapter;


    public SpinnerItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpinnerItemView(Context context) {
        super(context);
    }

    @Override
    public void initView() {
        spinnerViewItem = (Spinner) findViewById(R.id.spinnerItem);
        tvTitle = (TextView) findViewById(R.id.textViewItemLabel);
        ivValidateIcon = (ImageView) findViewById(R.id.ivSpinnerChecked);
        childrenLayout = (LinearLayout) findViewById(R.id.childLayout);

    }

    @Override
    public void setItem(Item item) {
        super.setItem(item);
        tvTitle.setText(item.getLabelForView());
        setSpinnerSelectList(item);

    }

    @Override
    public AudioFormData createAudioFormData() {
        AudioFormData data = new AudioFormData();
        data.setFormid(item.getFormid());
        Value selectedValue = (Value) spinnerViewItem.getSelectedItem();
        if (selectedValue != null) {
            data.setValue(selectedValue.getValue());
            data.setText(selectedValue.getText());
        }
        return data;
    }

    private void setSpinnerSelectList(Item item) {
        List<Value> listValues = item.getListvalue();
        if (listValues != null && listValues.size() > 0) {
            Value holder = listValues.get(0);
            if (!Utility.EMPTY_STRING.equals(holder.getValue())) {
                String placeHolder = item.getPlaceholder();
                if (placeHolder == null) {
                    holder = new Value("", "");
                } else {
                    holder = new Value("", placeHolder);
                }
                listValues.add(0, holder);
            }
            spinArrayAdapter = new SpinnerArrayAdapter(
                    this.getContext(), R.layout.spinner_item,
                    R.layout.spinner_title_bar, R.id.textViewSpinnerItem,
                    listValues, item.isRequired());
            spinnerViewItem.setAdapter(spinArrayAdapter);
            spinnerViewItem.setOnItemSelectedListener(this);
        }
    }

    @Override
    public void setValue(String value) {
        int position = spinArrayAdapter.findItemPosition(value);
        spinnerViewItem.setSelection(position);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        hideKeyboard();
        if (position == 0) {
            setValidated(false);
            return;
        } else {
            setValidated(true);
        }

        Value selectedValue = (Value) parent.getAdapter().getItem(
                position);
        if (view != null) {
            view.setTag(selectedValue);
        }
        Log.v(TAG, "GET VALUE: " + selectedValue.toString());
        // Remove child View and child Item
        removeAllChildItemViews();
        List<Item> items = selectedValue.getItems();
        if (items != null && items.size() > 0) {
            addChildItemView(items);
        }
        //handle check all item valid
        onValueChangedListener.onValueChanged(item.getFormid(), selectedValue);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //do nothing
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }
}
