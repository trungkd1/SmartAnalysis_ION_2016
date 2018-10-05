package jp.co.fujixerox.sa.ion.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import jp.co.fujixerox.sa.ion.entities.Value;

public class SpinnerArrayAdapter extends ArrayAdapter<Value> {
    private static final String TAG = SpinnerArrayAdapter.class
            .getSimpleName();
    private Context mContext;
    private List<Value> mListValue;
    private boolean isSelected = false;
    private boolean mIsRequired = false;
    private int mItemLayoutId;
    private int mItemTextViewId;
    private int mItemTitleBarId;

    public SpinnerArrayAdapter(Context context, int itemResId, int titleBarResId,
                               int textViewId, List<Value> listValues, boolean isRequired) {
        super(context, itemResId, textViewId, listValues);
        this.mContext = context;
        this.mListValue = listValues;
        this.mItemLayoutId = itemResId;
        this.mItemTextViewId = textViewId;
        this.mItemTitleBarId = titleBarResId;
        this.mIsRequired = isRequired;
    }

    public SpinnerArrayAdapter(Context context, int itemResId,
                               List<Value> listValues, boolean isRequired) {
        super(context, itemResId, listValues);
        this.mContext = context;
        this.mListValue = listValues;
        this.mItemLayoutId = itemResId;
        this.mIsRequired = isRequired;
    }

    @Override
    public int getCount() {
        if (mListValue == null || mListValue.size() <= 0) {
            Log.e(TAG, "@list value is null!");
            return 0;
        }
        return mListValue.size();
    }

    @Override
    public Value getItem(int position) {
        return mListValue.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int findItemPosition(String value) {
        int result = 0;
        int count = getCount();
        for (int i = 0; i < count; i++) {
            Value valueItem = getItem(i);
            if (!(valueItem == null)) {
                if (TextUtils.equals(valueItem.getValue(), value)) {
                    result = i;
                    break;
                }
            }
        }
        return result;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = null;
        Value value = mListValue.get(position);
        if (value == null || TextUtils.isEmpty(value.getValue())) {
            view = setItemView(position, inflater, mItemTitleBarId);
        } else {
            view = setItemView(position, inflater, mItemLayoutId);
        }
        return view;
    }

    private View setItemView(int position, LayoutInflater inflater, int layoutId) {
        View view = inflater.inflate(layoutId, null);
        TextView textViewSpinnerItem = (TextView) view
                .findViewById(mItemTextViewId);
        Value value = mListValue.get(position);
        Log.v(TAG, "SetItemView: " + value.getText());
        textViewSpinnerItem.setText(value.getText());
        return view;
    }

    @Override
    public boolean isEnabled(int position) {
        String value = mListValue.get(position).getText();
        return value != null && (position != 0 || !mIsRequired) && super.isEnabled(position);
    }
}
