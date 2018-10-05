package jp.co.fujixerox.sa.ion.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.entities.Item;
import jp.co.fujixerox.sa.ion.entities.Value;
import jp.co.fujixerox.sa.ion.db.AudioData;
import jp.co.fujixerox.sa.ion.db.AudioFormData;
import jp.co.fujixerox.sa.ion.utils.Utility;

/**
 * Created by TrungKD
 */
public abstract class AbstractItemView extends LinearLayout {
    private static final String TAG = AbstractItemView.class.getSimpleName();
    protected Item item;
    protected TextView tvTitle;
    protected ImageView ivValidateIcon;
    protected List<AbstractItemView> childrenItems;
    protected LayoutInflater mLayoutInflater;
    protected LinearLayout childrenLayout;
    protected OnValueChangedListener onValueChangedListener;
    protected AudioData audioData;
    public AbstractItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public AbstractItemView(Context context) {
        super(context, null);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initView();
    }

    public abstract void initView();


    public abstract void setValue(String value);

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getFormId() {
        return this.item.getFormid();
    }
    public boolean isRequired() {
        return item.isRequired();
    }

    public boolean isValidated() {
        if (isRequired()) {
            return ivValidateIcon.getVisibility() == VISIBLE;
        } else {
            return true;
        }
    }
    public void setValidated(boolean isValidated) {
        if (isValidated && isRequired()) {
            ivValidateIcon.setVisibility(VISIBLE);
        } else {
            ivValidateIcon.setVisibility(INVISIBLE);
        }
    }

    public void setAudioData(AudioData audioData) {
        this.audioData = audioData;
    }

    public void addChildItemView(List<Item> childItems) {
        childrenItems = new ArrayList<>();
        for (Item item:
                childItems) {
            String inputPattern = item.getPattern();
            AbstractItemView itemView = null;
            if (Utility.INPUT_PATTERN.SELECT.name().equals(inputPattern)) {
                itemView = (AbstractItemView) mLayoutInflater.inflate(R.layout.spinner_item_layout, null);
            } else if (Utility.INPUT_PATTERN.BOOL.name().equals(inputPattern)) {
                itemView = (AbstractItemView) mLayoutInflater.inflate(R.layout.radiobutton_item_layout, null);
            } else if (Utility.INPUT_PATTERN.OTHER.name().equals(inputPattern)) {

            } else {
                itemView = (AbstractItemView) mLayoutInflater.inflate(R.layout.edittext_item_layout, null);
            }
            //child item
            itemView.setOnValueChangedListener(onValueChangedListener);
            itemView.setItem(item);
            childrenLayout.addView(itemView);
            //parent item
            childrenItems.add(itemView);
        }
        //Set data value to ItemViews
        if (audioData != null) {
            List<AudioFormData> listAudioFormData = audioData.getListAudioFormData();
            for (AudioFormData audioFormData : listAudioFormData) {
                String formId = audioFormData.getFormid();
                for (AbstractItemView itemView : childrenItems) {
                    if (formId.equals(itemView.getFormId())) {
                        itemView.setValue(audioFormData.getValue());
                        break;
                    }
                }
            }
        }

    }

    public List<AbstractItemView> getChildItemViews() {
        return childrenItems;
    }
    /** Remove child View and child Item
     すべて古いChildViewを削除する
     */
    public void removeAllChildItemViews() {
        // number of subviews
        int childCount = childrenLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            // returning 0th position of child view
            AbstractItemView childView = (AbstractItemView) childrenLayout
                    .getChildAt(0); // 削除しているため、いつもIndex０で削除する。
            if (childView != null) {
                Item item = childView.getItem();
//todo                     mInputItemView.runRemoveInputChildView(item);
                childrenLayout.removeView(childView);
            }
        }
    }

    protected void hideKeyboard() {
        Log.v(TAG, "hidekeyboard");
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindowToken(), 0);
    }

    public abstract AudioFormData createAudioFormData();

    public void setOnValueChangedListener(OnValueChangedListener listener) {
        onValueChangedListener = listener;
    }
    public interface OnValueChangedListener {
        void onValueChanged(String formId, Value value);
    }


}
