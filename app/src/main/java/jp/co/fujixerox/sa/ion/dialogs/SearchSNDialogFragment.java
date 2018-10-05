package jp.co.fujixerox.sa.ion.dialogs;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.List;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.db.ICloudParams;
import jp.co.fujixerox.sa.ion.entities.Item;
import jp.co.fujixerox.sa.ion.entities.Value;
import jp.co.fujixerox.sa.ion.utils.CommonUtils;
import jp.co.fujixerox.sa.ion.utils.JsonParser;
import jp.co.fujixerox.sa.ion.utils.Utility;
import jp.co.fujixerox.sa.ion.views.AbstractItemView;
import jp.co.fujixerox.sa.ion.views.EditTextItemView;
import jp.co.fujixerox.sa.ion.views.SpinnerItemView;

/**
 * Created by TrungKD
 */

public class SearchSNDialogFragment extends DialogFragment implements View.OnClickListener {
    private static final String TAG = SearchSNDialogFragment.class.getSimpleName();
    private EditText etSN;
    private Button btnSearch;
    private OnSearchSNListener mCallback;
    private String productName;
    private List<Item> itemList;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnSearchSNListener) activity;
        } catch (ClassCastException ex) {
            throw new ClassCastException(activity.toString() + " must implement ONSearchSNListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layoutView = layoutInflater.inflate(R.layout.dialog_search_sn_layout, null);
        initGUI(layoutView);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.search_sn_report);
        builder.setView(layoutView);
        return builder.create();
    }

    private void initGUI(View layoutView) {
        etSN = (EditText) layoutView.findViewById(R.id.etSN);
        btnSearch = (Button) layoutView.findViewById(R.id.btnSearch);
        itemList = JsonParser.getListItems(
                Utility.ASSETS_JSON_PATH,
                Utility.JSON_FILE_NAME.AUDIO_CONDITIONS, getResources().getAssets());

        Item item;
        for (Item checkitem :itemList) {
            if (Utility.IS_BARCODE){
                if (ICloudParams.productname.equals(checkitem.getFormid())) {
                    item = checkitem;
                    createProductSelectViewVIABarcode(layoutView,item);
                }
            }else {
                if (ICloudParams.productgroup.equals(checkitem.getFormid())) {
                    item = checkitem;
                    createProductSelectView(layoutView,item);
                    break;
                }
            }
        }
        btnSearch.setOnClickListener(this);
    }

    /**
     * Create input form product
     * @param containView
     */
    private void createProductSelectView(final View containView, Item item) {
        final SpinnerItemView itemView = (SpinnerItemView) containView.findViewById(R.id.spinner);
        itemView.setVisibility(View.VISIBLE);
        if (item != null) {
            itemView.setItem(item);
            itemView.setOnValueChangedListener(new AbstractItemView.OnValueChangedListener() {
                @Override
                public void onValueChanged(String formId, Value value) {
                    if (ICloudParams.productname.equals(formId)) {
                        productName = value.getValue();
                    }
                }
            });
        } else {
            Log.e(TAG, "Can't get products from assets");
        }
    }


    /**
     * Create input form product via version Barcode
     * @param containView
     */
    private void createProductSelectViewVIABarcode(final View containView, Item item) {
        final EditTextItemView itemView = (EditTextItemView) containView.findViewById(R.id.edittext);
        itemView.setVisibility(View.VISIBLE);
        if (item != null) {
            itemView.setItem(item);
            itemView.setOnValueChangedListener(new AbstractItemView.OnValueChangedListener() {
                @Override
                public void onValueChanged(String formId, Value value) {
                    if (ICloudParams.productname.equals(formId)) {
                        productName = value.getValue();
                    }
                }
            });
//            spinnerItemView.setOnValueChangedListener(new AbstractItemView.OnValueChangedListener() {
//                @Override
//                public void onValueChanged(String formId, Value value) {
//                    if (ICloudParams.productname.equals(formId)) {
//                        productName = value.getValue();
//                    }
//                }
//            });
        } else {
            Log.e(TAG, "Can't get products from assets");
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == btnSearch.getId()) {
            if (isValidate()) {
                mCallback.onSearchReportBySerialId(etSN.getText().toString(), productName);
                dismiss();
            }
        }
    }

    private boolean isValidate() {
        if(TextUtils.isEmpty(productName)){
            CommonUtils.showToast(getContext(), R.string.catalog_not_found);
            return false;
        }
        String searchText = etSN.getText().toString();
        if (searchText.isEmpty()) {
            CommonUtils.showToast(getContext(), R.string.input_not_empty_allowed);
            return false;
        } else if (searchText.length() < 6) {
            CommonUtils.showToast(getContext(), R.string.input_atleast_6_characters);
            return false;
        }
        return true;
    }


    public interface OnSearchSNListener{
       void onSearchReportBySerialId(String serialId, String productName);
    }
}
