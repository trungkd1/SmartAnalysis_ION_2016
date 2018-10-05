package jp.co.fujixerox.sa.ion.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import jp.co.fujixerox.sa.ion.R;

/**
 * 端末にログインされたGoogleアカウントがないため、
 * アカウント管理画面を開く確認のダイアログである。
 */
public class CatalogSelectDialog extends DialogFragment {
    private View.OnClickListener onClickListener;

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_select_catalog, null);
        view.findViewById(R.id.catalog_list_btn).setOnClickListener(onClickListener);
        view.findViewById(R.id.catalog_select_btn).setOnClickListener(onClickListener);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
        .setTitle(R.string.catalog_view_type);
        // Create the AlertDialog object and return it
        return builder.create();
    }
}