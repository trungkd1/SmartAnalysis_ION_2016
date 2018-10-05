package jp.co.fujixerox.sa.ion.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;

import java.util.List;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.entities.Item;
import jp.co.fujixerox.sa.ion.entities.Value;
import jp.co.fujixerox.sa.ion.utils.JsonParser;
import jp.co.fujixerox.sa.ion.utils.Utility;

/**
 * Created by fxstdpc-admin on 2015/12/03.
 * Catalog filter dialog
 */
public class CatalogFilterDialogFragment extends DialogFragment {
    private static final String TAG = CatalogFilterDialogFragment.class.getSimpleName();
    private FilterProfile mFilterProfile;
    private OnFilterChangeListener mListener;
    private  Dialog dialog;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.v(TAG, "onCreateDialog");
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.filter_select_layout, null);
        setDialogContentView(view);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Specify the list array, the items to be selected by default (null for none),
        // and the mListener through which to receive callbacks when items are selected
        builder.setView(view)
            // Set the action buttons
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK, so save the mSelectedItems results somewhere
                    // or return them to the component that opened the dialog
                   mListener.onFilterChanged(mFilterProfile);
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    //...
                }
            });
        dialog = builder.create();
        return dialog;
    }

    /**
     * Dialog contentView setting
     * @param contentView
     */
    private void setDialogContentView(View contentView) {
        ListView listView = (ListView) contentView.findViewById(R.id.filterSelectList);
        CheckBox checkBox = (CheckBox) contentView.findViewById(R.id.cb_selectAll);
        FilterListArrayAdapter arrayAdapter = new FilterListArrayAdapter(getContext(),
                R.layout.item_checkbox, checkBox);
        listView.setAdapter(arrayAdapter);
    }


    @Override
    public void onResume() {
        super.onResume();
    }


    public void setOnFilterChangeListener(OnFilterChangeListener listener) {
        this.mListener = listener;
    }

    public void setFilterProfile(FilterProfile filterProfile) {
        this.mFilterProfile = filterProfile;
    }

    public interface OnFilterChangeListener {
        void onFilterChanged(FilterProfile filterProfile);
    }

    private class FilterListArrayAdapter extends ArrayAdapter<String> {
        private LayoutInflater inflater;
        private int itemLayoutId;
        private CheckBox mSelectAllCheckbox;

        public FilterListArrayAdapter(Context context, int itemResId, final CheckBox selectAllCheckbox) {
            super(context, itemResId, mFilterProfile.getCheckStringArray());
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.itemLayoutId = itemResId;
            this.mSelectAllCheckbox = selectAllCheckbox;
            selectAllCheckbox.setChecked(mFilterProfile.isAllChecked());
            selectAllCheckbox.setOnClickListener(new CompoundButton.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setAllChecked(selectAllCheckbox.isChecked());
                }
            });
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = inflater.inflate(itemLayoutId, null);
            }
            CheckBox checkBox = (CheckBox) view;
            //checkBox.setText(getItem(position));
            checkBox.setText(getTypeText(getItem(position)));
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isChecked = ((CheckBox) v).isChecked();
                    mFilterProfile.setChecked(position, isChecked);
                    mSelectAllCheckbox.setChecked(mFilterProfile.isAllChecked());
                }
            });
            boolean value = mFilterProfile.getCheckedList()[position];
            ((CheckBox)view).setChecked(value);
            return view;
        }
        /*
        TypeのValueをJsonからText値に変換 160510 mit
        */
        private String getTypeText(String typeValue){
            String typeText = typeValue;
            AssetManager assetManager = getActivity().getAssets();
            List<Item> mItems = JsonParser.getListItems(
                    Utility.ASSETS_JSON_PATH,
                    Utility.JSON_FILE_NAME.CATALOG_SEARCH, assetManager);
            for(Item item : mItems){
                if(item.getFormid().equals("type")){
                    for(Value val : item.getListvalue() ){
                        if(typeValue.equals(val.getValue())) {
                            typeText = val.getText();
                            break;
                        }
                    }
                }
            }
            return typeText;
        }

        public void setAllChecked(boolean isChecked) {
            mFilterProfile.setAllChecked(isChecked);
            notifyDataSetChanged();
        }

    }


}
