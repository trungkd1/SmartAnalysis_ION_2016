package jp.co.fujixerox.sa.ion.views;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.db.AudioData;
import jp.co.fujixerox.sa.ion.db.AudioFormData;
import jp.co.fujixerox.sa.ion.db.ICloudParams;
import jp.co.fujixerox.sa.ion.entities.Item;
import jp.co.fujixerox.sa.ion.utils.Utility;

/**
 * Created by TrungKD
 */
public class ItemViewGenerator {
    private static final String TAG = "ItemViewGenerator";


    private LayoutInflater mLayoutInflater;
    private List<AbstractItemView> itemViews = new ArrayList<>();

    public ItemViewGenerator(Context context) {
        mLayoutInflater =  (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setSectionInputLayout(List<String> formIds, List<Item> items,
                                      LinearLayout contentLayout, LinearLayout areaSectionLayout, AudioData audioData,
                                      AbstractItemView.OnValueChangedListener listener) {
        //Create ItemViews from JsonItem
        for (Item item: items) {
            String inputPattern = item.getPattern();
            AbstractItemView itemView;
            if (!formIds.contains(item.getFormid())) {
                continue;
            }
            if (Utility.INPUT_PATTERN.SELECT.name().equals(inputPattern)) {
                itemView = (AbstractItemView) mLayoutInflater.inflate(R.layout.spinner_item_layout, null);
                contentLayout.addView(itemView);
            } else if (Utility.INPUT_PATTERN.BOOL.name().equals(inputPattern)) {
                itemView = (AbstractItemView) mLayoutInflater.inflate(R.layout.radiobutton_item_layout, null);
                contentLayout.addView(itemView);
            } else if (Utility.INPUT_PATTERN.RADIO.name().equals(inputPattern)) {
                itemView = (AbstractItemView) mLayoutInflater.inflate(R.layout.radio4button_item_layout, null);
                contentLayout.addView(itemView);
            } else if (Utility.INPUT_PATTERN.OTHER.name().equals(inputPattern)) {
                if (areaSectionLayout != null) {
                    itemView = (AbstractItemView) mLayoutInflater.inflate(R.layout.areacells_item_layout, null);
                    areaSectionLayout.addView(itemView);
                } else {
                    continue;
                }
            }else if (Utility.INPUT_PATTERN.CHECK.name().equals(inputPattern)) {
                itemView = (AbstractItemView) mLayoutInflater.inflate(R.layout.layout_cause_parts, null);
                if ( item.getFormid().equals(ICloudParams.cause)){
                    ((CheckItemView)itemView).setTreatmentView(ICloudParams.cause,audioData.getCause(),audioData.getCasuseJsonForm());
                    contentLayout.addView(itemView);
                }else if ( item.getFormid().equals(ICloudParams.method)){
                    ((CheckItemView)itemView).setTreatmentView(ICloudParams.method,audioData.getMethod(), audioData.getMethodJsonForm());
                    contentLayout.addView(itemView);
                }

            } else {
                itemView = (AbstractItemView) mLayoutInflater.inflate(R.layout.edittext_item_layout, null);
                contentLayout.addView(itemView);
            }
            itemView.setOnValueChangedListener(listener);
            itemView.setItem(item);
            itemViews.add(itemView);
        }

        //Set data value to ItemViews
        if (audioData != null) {
            List<AudioFormData> listAudioFormData = audioData.getListAudioFormData();
            for (AudioFormData audioFormData : listAudioFormData) {
                String formId = audioFormData.getFormid();

                Log.v(TAG, "formid="+formId+"; value="+audioFormData.getValue());
                for (AbstractItemView itemView : itemViews) {
                    if (formId.equals(itemView.getFormId())) {
                        itemView.setValue(audioFormData.getValue());
                        itemView.setAudioData(audioData);
                        break;
                    }

                }
            }

        }
    }

    /**
     * Create AudioFormData from list of ItemViews
     * @return List<AudioFormData>
     */
    public List<AudioFormData> getAudioFormData() {
        List<AudioFormData> result = new ArrayList<>();
        List<AbstractItemView> listItemViews = geAllItemViews();
        for (AbstractItemView itemview:
             listItemViews) {
            AudioFormData audioFormData = itemview.createAudioFormData();
            if (audioFormData != null) {
                result.add(audioFormData);
            }
        }
        return result;
    }


    /**
     * Get all ItemViews include children items
     */
    public List<AbstractItemView> geAllItemViews() {
        List<AbstractItemView> result = new ArrayList<>();
        for (AbstractItemView itemView: itemViews) {
            result.add(itemView);
            getChildItemViews(itemView, result);
        }
        return result;
    }

    /**
     * Recurse to get all child ItemView
     */
    private void getChildItemViews(AbstractItemView itemView, List<AbstractItemView> list) {
        List<AbstractItemView> childList = itemView.getChildItemViews();
        if (childList != null && childList.size() > 0) {
            list.addAll(childList);
            for (AbstractItemView childItemView: childList) {
                List<AbstractItemView> children = childItemView.getChildItemViews();
                if (children != null && children.size() > 0) {
                    list.addAll(children);
                    getChildItemViews(childItemView, list);
                }
            }
        }
    }

    /**
     * Check is all required items are imputed
     */
    public boolean isValidated() {
        List<AbstractItemView> list = geAllItemViews();
        for (AbstractItemView item:
             list) {
            if (!item.isValidated()) {
                return false;
            }
        }
        return true;
    }

}
